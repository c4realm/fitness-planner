const db = require('../config/db');
const { AppError } = require('../middleware/errorHandler');

exports.getAll = async (req, res, next) => {
  try {
    const result = await db.query(
      `SELECT ws.*, w.name AS workout_name
       FROM workout_sessions ws
       JOIN workouts w ON w.workout_id = ws.workout_id
       WHERE ws.user_id = $1
       ORDER BY ws.started_at DESC`,
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
      `SELECT ws.*, w.name AS workout_name
       FROM workout_sessions ws
       JOIN workouts w ON w.workout_id = ws.workout_id
       WHERE ws.session_id = $1 AND ws.user_id = $2`,
      [req.params.id, req.user.user_id]
    );
    if (result.rows.length === 0) {
      throw new AppError('Session not found', 404);
    }
    res.json({ status: 'success', data: result.rows[0] });
  } catch (err) {
    next(err);
  }
};

exports.create = async (req, res, next) => {
  try {
    const { workout_id, started_at, notes } = req.body;

    if (!workout_id) throw new AppError('workout_id is required', 400);

    const result = await db.query(
      `INSERT INTO workout_sessions (user_id, workout_id, started_at, notes)
       VALUES ($1, $2, $3, $4)
       RETURNING *`,
      [req.user.user_id, workout_id, started_at || new Date(), notes]
    );
    res.status(201).json({ status: 'success', data: result.rows[0] });
  } catch (err) {
    next(err);
  }
};

exports.complete = async (req, res, next) => {
  try {
    const { ended_at, duration_min, notes } = req.body;

    const result = await db.query(
      `UPDATE workout_sessions
       SET ended_at = $1,
           duration_min = $2,
           notes = COALESCE($3, notes)
       WHERE session_id = $4 AND user_id = $5
       RETURNING *`,
      [ended_at || new Date(), duration_min, notes, req.params.id, req.user.user_id]
    );
    if (result.rows.length === 0) {
      throw new AppError('Session not found', 404);
    }
    res.json({ status: 'success', data: result.rows[0] });
  } catch (err) {
    next(err);
  }
};

exports.remove = async (req, res, next) => {
  try {
    const result = await db.query(
      'DELETE FROM workout_sessions WHERE session_id = $1 AND user_id = $2 RETURNING session_id',
      [req.params.id, req.user.user_id]
    );
    if (result.rows.length === 0) {
      throw new AppError('Session not found', 404);
    }
    res.json({ status: 'success', message: 'Session deleted' });
  } catch (err) {
    next(err);
  }
};
