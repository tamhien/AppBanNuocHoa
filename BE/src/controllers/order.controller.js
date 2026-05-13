const db = require('../config/db');

exports.checkout = async (req, res) => {
    const { user_id, selected_cart_ids, total_amount, payment_method, recipient_name, recipient_phone, recipient_address, note } = req.body;

    console.log(">>> [ORDER] Request received:", req.body);

    if (!user_id || !selected_cart_ids || !Array.isArray(selected_cart_ids) || selected_cart_ids.length === 0) {
        return res.status(200).json({ success: false, message: "Dữ liệu không hợp lệ hoặc không có sản phẩm được chọn" });
    }

    const connection = await db.getConnection();
    await connection.beginTransaction();

    try {
        // 1. Tạo Đơn hàng
        const [orderResult] = await connection.query(
            "INSERT INTO Orders (user_id, total_amount, payment_method, recipient_name, recipient_phone, recipient_address, note) VALUES (?, ?, ?, ?, ?, ?, ?)",
            [user_id, total_amount, payment_method, recipient_name, recipient_phone, recipient_address, note]
        );
        const orderId = orderResult.insertId;

        // 2. Chốt chi tiết đơn hàng
        // Sử dụng cách join trực tiếp từ Cart sang Perfumes để lấy giá, tránh loop query nhiều lần
        const [itemsToOrder] = await connection.query(`
            SELECT c.perfume_id, c.quantity, p.price
            FROM Cart c
            JOIN Perfumes p ON c.perfume_id = p.perfume_id
            WHERE c.cart_id IN (?)
        `, [selected_cart_ids]);

        if (itemsToOrder.length === 0) {
            throw new Error("Không tìm thấy sản phẩm hợp lệ trong giỏ hàng");
        }

        for (let item of itemsToOrder) {
            // Lưu vào OrderDetails
            await connection.query(
                "INSERT INTO OrderDetails (order_id, perfume_id, quantity, unit_price) VALUES (?, ?, ?, ?)",
                [orderId, item.perfume_id, item.quantity, item.price]
            );

            // Cập nhật kho
            await connection.query(
                "UPDATE Perfumes SET stock_quantity = stock_quantity - ? WHERE perfume_id = ?",
                [item.quantity, item.perfume_id]
            );
        }

        // 3. Xóa các sản phẩm đã thanh toán khỏi Cart
        await connection.query("DELETE FROM Cart WHERE cart_id IN (?)", [selected_cart_ids]);

        await connection.commit();
        res.status(200).json({ success: true, order_id: orderId, message: "Đặt hàng thành công!" });

    } catch (err) {
        await connection.rollback();
        console.error(">>> [ORDER] Error:", err.message);
        res.status(200).json({ success: false, message: "Lỗi Server: " + err.message });
    } finally {
        connection.release();
    }
};

exports.getUserOrders = async (req, res) => {
    try {
        const userId = req.params.userId;
        const { status } = req.query;

        let query = "SELECT * FROM Orders WHERE user_id = ?";
        let params = [userId];

        if (status && status !== "All") {
            query += " AND status = ?";
            params.push(status);
        }

        query += " ORDER BY order_date DESC";

        const [orders] = await db.query(query, params);
        for (let order of orders) {
            const [details] = await db.query(`
                SELECT od.*, p.name, p.image_url, p.brand
                FROM OrderDetails od
                JOIN Perfumes p ON od.perfume_id = p.perfume_id
                WHERE od.order_id = ?
            `, [order.order_id]);
            order.items = details;
        }
        res.json(orders);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.getAllOrders = async (req, res) => {
    try {
        const [orders] = await db.query("SELECT o.*, u.full_name FROM Orders o JOIN Users u ON o.user_id = u.user_id ORDER BY o.order_date DESC");
        for (let order of orders) {
            const [details] = await db.query(`
                SELECT od.*, p.name, p.image_url FROM OrderDetails od JOIN Perfumes p ON od.perfume_id = p.perfume_id WHERE od.order_id = ?
            `, [order.order_id]);
            order.items = details;
        }
        res.json(orders);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.updateOrderStatus = async (req, res) => {
    const { order_id, status } = req.body;
    try {
        await db.query("UPDATE Orders SET status = ? WHERE order_id = ?", [status, order_id]);
        res.json({ success: true, message: "Cập nhật thành công" });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.getRevenue = async (req, res) => {
    try {
        const [revenue] = await db.query("SELECT SUM(total_amount) as total_revenue, COUNT(*) as order_count FROM Orders WHERE status = 'Completed'");
        res.json({ success: true, total_revenue: revenue[0].total_revenue || 0, order_count: revenue[0].order_count || 0 });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
