const jwt = require('jsonwebtoken');
const { AppError } = require('./errorHandler');

const authenticate = (req, _res, next) => {
  const authHeader = req.headers.authorization;

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return next(new AppError('Authentication required — missing or malformed Authorization header', 401));
  }

  const token = authHeader.split(' ')[1];

  jwt.verify(token, process.env.JWT_SECRET, (err, decoded) => {
    if (err) {
      if (err.name === 'TokenExpiredError') {
        return next(new AppError('Session expired — please sign in again', 401));
      }
      return next(new AppError('Invalid or tampered token', 401));
    }

    req.user = { user_id: decoded.user_id };
    next();
  });
};

module.exports = { authenticate };
