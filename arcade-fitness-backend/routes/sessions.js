const { Router } = require('express');
const { authenticate } = require('../middleware/auth');
const ctrl = require('../controllers/sessionController');

const router = Router();

router.use(authenticate);

router.get('/', ctrl.getAll);
router.get('/:id', ctrl.getById);
router.post('/', ctrl.create);
router.put('/:id/complete', ctrl.complete);
router.delete('/:id', ctrl.remove);

module.exports = router;
