const db = require('../config/db');

exports.checkout = async (req, res) => {
    const { user_id, selected_cart_ids, total_amount, payment_method, recipient_name, recipient_phone, recipient_address } = req.body;
    try {
        // 1. Tạo Order
        const [orderResult] = await db.query(
            "INSERT INTO Orders (user_id, total_amount, payment_method, recipient_name, recipient_phone, recipient_address) VALUES (?, ?, ?, ?, ?, ?)",
            [user_id, total_amount, payment_method, recipient_name, recipient_phone, recipient_address]
        );
        const orderId = orderResult.insertId;

        // 2. Chuyển từ Cart sang OrderDetails
        const [cartItems] = await db.query("SELECT * FROM Cart WHERE cart_id IN (?)", [selected_cart_ids]);
        for (let item of cartItems) {
            const [p] = await db.query("SELECT price FROM Perfumes WHERE perfume_id = ?", [item.perfume_id]);
            await db.query(
                "INSERT INTO OrderDetails (order_id, perfume_id, quantity, unit_price) VALUES (?, ?, ?, ?)",
                [orderId, item.perfume_id, item.quantity, p[0].price]
            );
            // Trừ kho
            await db.query("UPDATE Perfumes SET stock_quantity = stock_quantity - ? WHERE perfume_id = ?", [item.quantity, item.perfume_id]);
        }

        // 3. Xóa món đã mua khỏi giỏ
        await db.query("DELETE FROM Cart WHERE cart_id IN (?)", [selected_cart_ids]);

        res.json({ success: true, order_id: orderId });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// Admin: Lấy tất cả đơn hàng
exports.getAllOrders = async (req, res) => {
    try {
        const query = `
            SELECT o.*, u.full_name
            FROM Orders o
            JOIN Users u ON o.user_id = u.user_id
            ORDER BY o.order_date DESC
        `;
        const [rows] = await db.query(query);
        res.json(rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// Admin: Cập nhật trạng thái
exports.updateOrderStatus = async (req, res) => {
    const { order_id, status } = req.body;
    try {
        await db.query("UPDATE Orders SET status = ? WHERE order_id = ?", [status, order_id]);
        res.json({ success: true, message: "Cập nhật trạng thái thành công" });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// Admin: Thống kê doanh thu
exports.getRevenue = async (req, res) => {
    try {
        const [revenue] = await db.query("SELECT SUM(total_amount) as total_revenue, COUNT(*) as order_count FROM Orders WHERE status = 'Completed'");
        res.json({
            success: true,
            total_revenue: revenue[0].total_revenue || 0,
            order_count: revenue[0].order_count || 0
        });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// Xem lịch sử đơn hàng của User
exports.getUserOrders = async (req, res) => {
    try {
        const [rows] = await db.query("SELECT * FROM Orders WHERE user_id = ? ORDER BY order_date DESC", [req.params.userId]);
        res.json(rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};