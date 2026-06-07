const { Router } = require('express');
const { authenticate } = require('../middleware/auth');
const ctrl = require('../controllers/exerciseController');

const router = Router();

router.use(authenticate);

router.get('/', ctrl.getAll);

module.exports = router;
