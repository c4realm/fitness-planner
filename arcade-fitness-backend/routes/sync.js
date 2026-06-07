const { Router } = require('express');
const { authenticate } = require('../middleware/auth');
const ctrl = require('../controllers/syncController');

const router = Router();

router.use(authenticate);

router.post('/', ctrl.batchSync);

module.exports = router;
