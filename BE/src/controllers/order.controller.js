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
            // Kiểm tra tồn kho trước khi thanh toán
            const [stockRows] = await connection.query("SELECT stock_quantity, name FROM Perfumes WHERE perfume_id = ?", [item.perfume_id]);
            if (stockRows.length === 0 || stockRows[0].stock_quantity < item.quantity) {
                throw new Error(`Sản phẩm ${stockRows[0]?.name || ''} không đủ số lượng trong kho`);
            }

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
        const { month } = req.query; // format: YYYY-MM

        // 1. Thống kê theo từng tháng: Lấy doanh thu bằng cách sum(quantity * unit_price) từ OrderDetails
        const [monthlyStats] = await db.query(`
            SELECT
                DATE_FORMAT(o.order_date, '%Y-%m') as month,
                SUM(od.quantity * od.unit_price) as monthly_revenue,
                COUNT(DISTINCT o.order_id) as order_count
            FROM Orders o
            JOIN OrderDetails od ON o.order_id = od.order_id
            WHERE LOWER(o.status) LIKE '%completed%'
            GROUP BY month
            ORDER BY month DESC
        `);

        // 2. Lấy chi tiết đơn hàng
        let ordersDetail = [];
        if (month) {
            const [rows] = await db.query(`
                SELECT o.*, u.full_name
                FROM Orders o
                JOIN Users u ON o.user_id = u.user_id
                WHERE DATE_FORMAT(o.order_date, '%Y-%m') = ?
                AND LOWER(o.status) LIKE '%completed%'
                ORDER BY o.order_date DESC
            `, [month]);

            for (let order of rows) {
                const [details] = await db.query(`
                    SELECT od.*, p.name, p.image_url, p.brand
                    FROM OrderDetails od
                    JOIN Perfumes p ON od.perfume_id = p.perfume_id
                    WHERE od.order_id = ?
                `, [order.order_id]);

                // Đảm bảo items luôn là mảng, kể cả khi không có dữ liệu
                order.items = details || [];

                // Tính toán lại tổng tiền của từng đơn hàng từ chi tiết để đảm bảo chính xác
                order.total_amount = (details || []).reduce((sum, item) => sum + (parseFloat(item.quantity || 0) * parseFloat(item.unit_price || 0)), 0);
            }
            ordersDetail = rows;
        }

        // 3. Tính toán Tổng doanh thu và Tổng số đơn hiển thị lên UI
        let displayRevenue = 0;
        let displayOrderCount = 0;

        if (month) {
            // Nếu chọn tháng: Tính tổng từ các đơn hàng đã lấy của tháng đó
            displayOrderCount = ordersDetail.length;
            displayRevenue = ordersDetail.reduce((sum, order) => sum + parseFloat(order.total_amount || 0), 0);
        } else {
            // Nếu không chọn tháng: Tính tổng từ tất cả các tháng
            displayOrderCount = monthlyStats.reduce((sum, stat) => sum + parseInt(stat.order_count || 0), 0);
            displayRevenue = monthlyStats.reduce((sum, stat) => sum + parseFloat(stat.monthly_revenue || 0), 0);
        }

        res.json({
            success: true,
            total_revenue: displayRevenue,
            order_count: displayOrderCount,
            monthly_stats: monthlyStats,
            orders_detail: ordersDetail
        });
    } catch (err) {
        res.status(500).json({ success: false, error: err.message });
    }
};
