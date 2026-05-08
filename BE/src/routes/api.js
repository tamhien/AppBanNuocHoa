const express = require('express');
const router = express.Router();
const auth = require('../controllers/auth.controller');
const perfume = require('../controllers/perfume.controller');
const order = require('../controllers/order.controller');
const upload = require('../controllers/upload.controller');

// Auth
router.post('/login', auth.login);
router.post('/register', auth.register);

// Perfumes
router.get('/perfumes', perfume.getAllPerfumes);
router.get('/perfumes/:id', perfume.getPerfumeById);
router.post('/perfumes', perfume.addPerfume); // Admin
router.put('/perfumes/:id', perfume.updatePerfume); // Admin
router.delete('/perfumes/:id', perfume.deletePerfume); // Admin

// Orders
router.post('/checkout', order.checkout);
router.get('/orders/user/:userId', order.getUserOrders);

// Admin - Orders
router.get('/admin/orders', order.getAllOrders);
router.put('/admin/orders/status', order.updateOrderStatus);
router.get('/admin/revenue', order.getRevenue);

// Admin - Users
router.get('/admin/users', auth.getAllUsers);
router.delete('/admin/users/:id', auth.deleteUser);

// Upload ảnh
router.post('/upload', upload.uploadImage);

module.exports = router;
