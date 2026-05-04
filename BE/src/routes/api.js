const express = require('express');
const router = express.Router();
const auth = require('../controllers/auth.controller');
const perfume = require('../controllers/perfume.controller');
const order = require('../controllers/order.controller');

// Auth
router.post('/login', auth.login);
router.post('/register', auth.register);

// Perfumes
router.get('/perfumes', perfume.getAllPerfumes);
router.get('/perfumes/:id', perfume.getPerfumeById);
router.post('/perfumes', perfume.addPerfume); // Admin

// Orders
router.post('/checkout', order.checkout);
router.get('/orders/user/:userId', order.getUserOrders);

module.exports = router;