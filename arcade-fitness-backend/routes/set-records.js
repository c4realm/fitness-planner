const { Router } = require('express');
const { authenticate } = require('../middleware/auth');
const ctrl = require('../controllers/setRecordController');

const router = Router();

router.use(authenticate);

router.get('/workout/:workoutId', ctrl.getByWorkout);
router.get('/session/:sessionId', ctrl.getBySession);
router.post('/', ctrl.create);
router.put('/:id', ctrl.update);
router.delete('/:id', ctrl.remove);

module.exports = router;
