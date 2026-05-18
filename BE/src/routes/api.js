const express = require('express');
const router = express.Router();
const auth = require('../controllers/auth.controller');
const perfume = require('../controllers/perfume.controller');
const order = require('../controllers/order.controller');
const upload = require('../controllers/upload.controller');
const favorite = require('../controllers/favorite.controller');
const cart = require('../controllers/cart.controller');
const review = require('../controllers/review.controller');
const payment = require('../controllers/payment.controller');

// Auth
router.post('/login', auth.login);
router.post('/register', auth.register);
router.post('/forgot-password', auth.forgotPassword);
router.post('/send-otp', auth.sendOTP);
router.post('/reset-password-otp', auth.resetPasswordWithOTP);

// Profile
router.get('/profile/:id', auth.getProfile);
router.put('/profile/:id', auth.updateProfile);
router.put('/change-password/:id', auth.changePassword);

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

// Favorites
router.post('/favorites', favorite.addFavorite);
router.get('/favorites/:userId', favorite.getFavorites);
router.delete('/favorites/:userId/:perfumeId', favorite.removeFavorite);

// Cart
router.post('/cart/add', cart.addToCart);
router.get('/cart/:userId', cart.getCartByUserId);
router.put('/cart/update', cart.updateCartQuantity);
router.delete('/cart/:cartId', cart.deleteCartItem);

// Reviews
router.post('/reviews', review.addReview);
router.get('/reviews/:perfumeId', review.getPerfumeReviews);

// Payments (VNPay)
router.post('/create_payment_url', payment.createVnpayUrl);
router.get('/vnpay_return', payment.vnpayReturn);
router.get('/vnpay_ipn', payment.vnpayIpn);

module.exports = router;
