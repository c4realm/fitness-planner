const db = require('../config/db');
const { AppError } = require('../middleware/errorHandler');

exports.get = async (req, res, next) => {
  try {
    const result = await db.query(
      `SELECT u.email, u.created_at AS registered_at, p.*
       FROM users u
       LEFT JOIN user_profiles p ON p.user_id = u.user_id
       WHERE u.user_id = $1`,
      [req.user.user_id]
    );
    if (result.rows.length === 0) {
      throw new AppError('Profile not found', 404);
    }
    res.json({ status: 'success', data: result.rows[0] });
  } catch (err) {
    next(err);
  }
};

exports.update = async (req, res, next) => {
  try {
    const { height_cm, initial_weight_kg, target_weight_kg, date_of_birth, gender } = req.body;

    const result = await db.query(
      `UPDATE user_profiles
       SET height_cm = COALESCE($1, height_cm),
           initial_weight_kg = COALESCE($2, initial_weight_kg),
           target_weight_kg = COALESCE($3, target_weight_kg),
           date_of_birth = COALESCE($4, date_of_birth),
           gender = COALESCE($5, gender),
           updated_at = NOW()
       WHERE user_id = $6
       RETURNING *`,
      [height_cm, initial_weight_kg, target_weight_kg, date_of_birth, gender, req.user.user_id]
    );
    if (result.rows.length === 0) {
      throw new AppError('Profile not found', 404);
    }
    res.json({ status: 'success', data: result.rows[0] });
  } catch (err) {
    next(err);
  }
};
