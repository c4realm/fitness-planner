const db = require('../config/db');

exports.getAll = async (req, res, next) => {
  try {
    const { muscle_group } = req.query;
    let query = 'SELECT * FROM exercises';
    const params = [];

    if (muscle_group) {
      query += ' WHERE muscle_group = $1';
      params.push(muscle_group);
    }

    query += ' ORDER BY muscle_group, name';

    const result = await db.query(query, params);
    res.json({ status: 'success', data: result.rows });
  } catch (err) {
    next(err);
  }
};
