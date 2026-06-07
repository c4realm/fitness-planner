const errorHandler = (err, req, res, _next) => {
  const statusCode = err.statusCode || 500;

  const payload = {
    status: 'error',
    statusCode,
    message: err.message || 'Internal Server Error',
  };

  if (process.env.NODE_ENV === 'production') {
    delete payload.stack;
  } else {
    payload.stack = err.stack;
  }

  console.error(`[ERROR] ${req.method} ${req.originalUrl} — ${err.message}`);

  res.status(statusCode).json(payload);
};

class AppError extends Error {
  constructor(message, statusCode = 400) {
    super(message);
    this.statusCode = statusCode;
    Error.captureStackTrace(this, this.constructor);
  }
}

module.exports = { errorHandler, AppError };
