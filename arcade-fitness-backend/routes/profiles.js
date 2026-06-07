const { Router } = require('express');
const { authenticate } = require('../middleware/auth');
const ctrl = require('../controllers/profileController');

const router = Router();

router.use(authenticate);

router.get('/', ctrl.get);
router.put('/', ctrl.update);

module.exports = router;
