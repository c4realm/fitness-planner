const db = require('../config/db');
const { AppError } = require('../middleware/errorHandler');

exports.getAll = async (req, res, next) => {
  try {
    const result = await db.query(
      'SELECT * FROM workouts WHERE user_id = $1 ORDER BY day_of_week, created_at',
      [req.user.user_id]
    );
    res.json({ status: 'success', data: result.rows });
  } catch (err) {
    next(err);
  }
};

exports.getById = async (req, res, next) => {
  try {
    const result = await db.query(
      'SELECT * FROM workouts WHERE workout_id = $1 AND user_id = $2',
      [req.params.id, req.user.user_id]
    );
    if (result.rows.length === 0) {
      throw new AppError('Workout not found', 404);
    }
    res.json({ status: 'success', data: result.rows[0] });
  } catch (err) {
    next(err);
  }
};

exports.create = async (req, res, next) => {
  try {
    const { name, description, day_of_week, is_template } = req.body;
    if (!name) throw new AppError('Workout name is required', 400);

    const result = await db.query(
      `INSERT INTO workouts (user_id, name, description, day_of_week, is_template)
       VALUES ($1, $2, $3, $4, $5)
       RETURNING *`,
      [req.user.user_id, name, description, day_of_week, is_template || false]
    );
    res.status(201).json({ status: 'success', data: result.rows[0] });
  } catch (err) {
    next(err);
  }
};

exports.update = async (req, res, next) => {
  try {
    const { name, description, day_of_week, is_template } = req.body;

    const result = await db.query(
      `UPDATE workouts
       SET name = COALESCE($1, name),
           description = COALESCE($2, description),
           day_of_week = COALESCE($3, day_of_week),
           is_template = COALESCE($4, is_template),
           updated_at = NOW()
       WHERE workout_id = $5 AND user_id = $6
       RETURNING *`,
      [name, description, day_of_week, is_template, req.params.id, req.user.user_id]
    );
    if (result.rows.length === 0) {
      throw new AppError('Workout not found', 404);
    }
    res.json({ status: 'success', data: result.rows[0] });
  } catch (err) {
    next(err);
  }
};

exports.remove = async (req, res, next) => {
  try {
    const result = await db.query(
      'DELETE FROM workouts WHERE workout_id = $1 AND user_id = $2 RETURNING workout_id',
      [req.params.id, req.user.user_id]
    );
    if (result.rows.length === 0) {
      throw new AppError('Workout not found', 404);
    }
    res.json({ status: 'success', message: 'Workout deleted' });
  } catch (err) {
    next(err);
  }
};
