const db = require('../config/db');
const { AppError } = require('../middleware/errorHandler');

exports.getByWorkout = async (req, res, next) => {
  try {
    const result = await db.query(
      `SELECT sr.*, e.name AS exercise_name
       FROM set_records sr
       JOIN exercises e ON e.exercise_id = sr.exercise_id
       WHERE sr.workout_id = $1 AND sr.user_id = $2
       ORDER BY sr.exercise_id, sr.set_number`,
      [req.params.workoutId, req.user.user_id]
    );
    res.json({ status: 'success', data: result.rows });
  } catch (err) {
    next(err);
  }
};

exports.getBySession = async (req, res, next) => {
  try {
    const result = await db.query(
      `SELECT sr.*, e.name AS exercise_name
       FROM set_records sr
       JOIN exercises e ON e.exercise_id = sr.exercise_id
       WHERE sr.session_id = $1 AND sr.user_id = $2
       ORDER BY sr.exercise_id, sr.set_number`,
      [req.params.sessionId, req.user.user_id]
    );
    res.json({ status: 'success', data: result.rows });
  } catch (err) {
    next(err);
  }
};

exports.create = async (req, res, next) => {
  try {
    const { workout_id, exercise_id, set_number, weight_kg, reps, rpe, completed } = req.body;

    if (!workout_id || !exercise_id || !set_number) {
      throw new AppError('workout_id, exercise_id, and set_number are required', 400);
    }

    const result = await db.query(
      `INSERT INTO set_records (user_id, workout_id, exercise_id, session_id, set_number, weight_kg, reps, rpe, completed)
       VALUES ($1, $2, $3, $4, $5, $6, $7, $8, $9)
       RETURNING *`,
      [req.user.user_id, workout_id, exercise_id, req.body.session_id, set_number, weight_kg, reps, rpe, completed || false]
    );
    res.status(201).json({ status: 'success', data: result.rows[0] });
  } catch (err) {
    next(err);
  }
};

exports.update = async (req, res, next) => {
  try {
    const { weight_kg, reps, rpe, completed } = req.body;

    const result = await db.query(
      `UPDATE set_records
       SET weight_kg = COALESCE($1, weight_kg),
           reps = COALESCE($2, reps),
           rpe = COALESCE($3, rpe),
           completed = COALESCE($4, completed)
       WHERE set_id = $5 AND user_id = $6
       RETURNING *`,
      [weight_kg, reps, rpe, completed, req.params.id, req.user.user_id]
    );
    if (result.rows.length === 0) {
      throw new AppError('Set record not found', 404);
    }
    res.json({ status: 'success', data: result.rows[0] });
  } catch (err) {
    next(err);
  }
};

exports.remove = async (req, res, next) => {
  try {
    const result = await db.query(
      'DELETE FROM set_records WHERE set_id = $1 AND user_id = $2 RETURNING set_id',
      [req.params.id, req.user.user_id]
    );
    if (result.rows.length === 0) {
      throw new AppError('Set record not found', 404);
    }
    res.json({ status: 'success', message: 'Set record deleted' });
  } catch (err) {
    next(err);
  }
};
