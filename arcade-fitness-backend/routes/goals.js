const { Router } = require('express');
const { authenticate } = require('../middleware/auth');
const ctrl = require('../controllers/goalController');

const router = Router();

router.use(authenticate);

router.get('/', ctrl.getAll);
router.post('/', ctrl.create);
router.put('/:id', ctrl.update);
router.delete('/:id', ctrl.remove);

module.exports = router;
