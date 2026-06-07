const db = require('../config/db');
const { AppError } = require('../middleware/errorHandler');

exports.batchSync = async (req, res, next) => {
  const client = await db.getClient();

  try {
    const { operations } = req.body;

    if (!Array.isArray(operations) || operations.length === 0) {
      throw new AppError('operations array is required and must be non-empty', 400);
    }

    await client.query('BEGIN');

    const results = [];

    for (const op of operations) {
      const { table, action, data } = op;

      if (!table || !action || !data) {
        throw new AppError('Each operation requires table, action, and data fields', 400);
      }

      await client.query(
        `INSERT INTO sync_queue (user_id, table_name, operation, payload, status, attempt_count)
         VALUES ($1, $2, $3, $4, 'pending', 0)`,
        [req.user.user_id, table, action.toUpperCase(), JSON.stringify(data)]
      );

      let query;
      switch (`${table}:${action}`) {
        case 'set_records:insert':
          query = `INSERT INTO set_records (user_id, workout_id, exercise_id, session_id, set_number, weight_kg, reps, rpe, completed)
                   VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)`;
          await client.query(query, [
            req.user.user_id, data.workout_id, data.exercise_id, data.session_id,
            data.set_number, data.weight_kg, data.reps, data.rpe, data.completed || false,
          ]);
          break;

        case 'set_records:update':
          query = `UPDATE set_records SET weight_kg = $1, reps = $2, rpe = $3, completed = $4
                   WHERE set_id = $5 AND user_id = $6`;
          await client.query(query, [
            data.weight_kg, data.reps, data.rpe, data.completed,
            data.set_id, req.user.user_id,
          ]);
          break;

        case 'workout_sessions:insert':
          query = `INSERT INTO workout_sessions (user_id, workout_id, started_at, ended_at, duration_min, notes)
                   VALUES ($1, $2, $3, $4, $5, $6)`;
          await client.query(query, [
            req.user.user_id, data.workout_id, data.started_at, data.ended_at,
            data.duration_min, data.notes,
          ]);
          break;

        case 'workouts:insert':
          query = `INSERT INTO workouts (user_id, name, description, day_of_week, is_template)
                   VALUES ($1, $2, $3, $4, $5)`;
          await client.query(query, [
            req.user.user_id, data.name, data.description, data.day_of_week, data.is_template || false,
          ]);
          break;

        case 'workouts:update':
          query = `UPDATE workouts SET name = $1, description = $2, day_of_week = $3, is_template = $4, updated_at = NOW()
                   WHERE workout_id = $5 AND user_id = $6`;
          await client.query(query, [
            data.name, data.description, data.day_of_week, data.is_template,
            data.workout_id, req.user.user_id,
          ]);
          break;

        case 'goals:insert':
          query = `INSERT INTO goals (user_id, goal_type, target_value, start_date, end_date)
                   VALUES ($1, $2, $3, $4, $5)`;
          await client.query(query, [
            req.user.user_id, data.goal_type, data.target_value, data.start_date, data.end_date,
          ]);
          break;

        case 'goals:update':
          query = `UPDATE goals SET target_value = $1, current_value = $2, achieved = $3, updated_at = NOW()
                   WHERE goal_id = $4 AND user_id = $5`;
          await client.query(query, [
            data.target_value, data.current_value, data.achieved, data.goal_id, req.user.user_id,
          ]);
          break;

        default:
          results.push({ table, action, status: 'skipped — unknown operation' });
          continue;
      }

      results.push({ table, action, status: 'committed' });
    }

    await client.query(
      `UPDATE sync_queue SET status = 'completed', processed_at = NOW()
       WHERE user_id = $1 AND status = 'pending'`,
      [req.user.user_id]
    );

    await client.query('COMMIT');

    res.status(200).json({
      status: 'success',
      message: `Processed ${operations.length} operation(s)`,
      data: results,
    });
  } catch (err) {
    await client.query('ROLLBACK');

    await db.query(
      `UPDATE sync_queue SET status = 'failed', error_message = $1, attempt_count = attempt_count + 1
       WHERE user_id = $2 AND status = 'pending'`,
      [err.message, req.user.user_id]
    ).catch(() => {});

    next(err);
  } finally {
    client.release();
  }
};
