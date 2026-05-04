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

// Xem lịch sử đơn hàng của User
exports.getUserOrders = async (req, res) => {
    try {
        const [rows] = await db.query("SELECT * FROM Orders WHERE user_id = ? ORDER BY order_date DESC", [req.params.userId]);
        res.json(rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};