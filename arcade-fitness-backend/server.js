require('dotenv').config();

const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const { errorHandler } = require('./middleware/errorHandler');

const authRoutes = require('./routes/auth');
const workoutRoutes = require('./routes/workouts');
const exerciseRoutes = require('./routes/exercises');
const setRecordRoutes = require('./routes/set-records');
const profileRoutes = require('./routes/profiles');
const goalRoutes = require('./routes/goals');
const sessionRoutes = require('./routes/sessions');
const syncRoutes = require('./routes/sync');

const app = express();

app.use(helmet());
app.use(cors());
app.use(express.json({ limit: '1mb' }));

app.get('/api/health', (_req, res) => {
  res.json({ status: 'healthy', timestamp: new Date().toISOString() });
});

app.use('/api/auth', authRoutes);
app.use('/api/workouts', workoutRoutes);
app.use('/api/exercises', exerciseRoutes);
app.use('/api/set-records', setRecordRoutes);
app.use('/api/profiles', profileRoutes);
app.use('/api/goals', goalRoutes);
app.use('/api/sessions', sessionRoutes);
app.use('/api/sync', syncRoutes);

app.use((_req, res) => {
  res.status(404).json({ status: 'error', statusCode: 404, message: 'Route not found' });
});

app.use(errorHandler);

const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
  console.log(`[Arcade Fitness] Server running on port ${PORT} — ${process.env.NODE_ENV || 'development'} mode`);
});

module.exports = app;
