const db = require('../config/db');
const { AppError } = require('../middleware/errorHandler');

exports.getAll = async (req, res, next) => {
  try {
    const result = await db.query(
      'SELECT * FROM goals WHERE user_id = $1 ORDER BY created_at DESC',
      [req.user.user_id]
    );
    res.json({ status: 'success', data: result.rows });
  } catch (err) {
    next(err);
  }
};

exports.create = async (req, res, next) => {
  try {
    const { goal_type, target_value, start_date, end_date } = req.body;

    if (!goal_type || target_value == null) {
      throw new AppError('goal_type and target_value are required', 400);
    }

    const result = await db.query(
      `INSERT INTO goals (user_id, goal_type, target_value, start_date, end_date)
       VALUES ($1, $2, $3, $4, $5)
       RETURNING *`,
      [req.user.user_id, goal_type, target_value, start_date || new Date(), end_date]
    );
    res.status(201).json({ status: 'success', data: result.rows[0] });
  } catch (err) {
    next(err);
  }
};

exports.update = async (req, res, next) => {
  try {
    const { target_value, current_value, achieved, end_date } = req.body;

    const result = await db.query(
      `UPDATE goals
       SET target_value = COALESCE($1, target_value),
           current_value = COALESCE($2, current_value),
           achieved = COALESCE($3, achieved),
           end_date = COALESCE($4, end_date),
           updated_at = NOW()
       WHERE goal_id = $5 AND user_id = $6
       RETURNING *`,
      [target_value, current_value, achieved, end_date, req.params.id, req.user.user_id]
    );
    if (result.rows.length === 0) {
      throw new AppError('Goal not found', 404);
    }
    res.json({ status: 'success', data: result.rows[0] });
  } catch (err) {
    next(err);
  }
};

exports.remove = async (req, res, next) => {
  try {
    const result = await db.query(
      'DELETE FROM goals WHERE goal_id = $1 AND user_id = $2 RETURNING goal_id',
      [req.params.id, req.user.user_id]
    );
    if (result.rows.length === 0) {
      throw new AppError('Goal not found', 404);
    }
    res.json({ status: 'success', message: 'Goal deleted' });
  } catch (err) {
    next(err);
  }
};
