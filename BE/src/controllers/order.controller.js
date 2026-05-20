const db = require('../config/db');
exports.checkout = async (req, res) => {
    const { user_id, selected_cart_ids, total_amount, payment_method, recipient_name, recipient_phone, recipient_address, note } = req.body;

    console.log("====================================================");
    console.log(">>> [NEW ORDER REQUEST] Time:", new Date().toLocaleString());
    console.log(">>> User ID:", user_id);
    console.log(">>> Method:", payment_method);
    console.log(">>> Cart IDs:", selected_cart_ids);
    console.log("====================================================");

    if (!user_id || !selected_cart_ids || !Array.isArray(selected_cart_ids) || selected_cart_ids.length === 0) {
        console.error(">>> [ORDER CHECKOUT] Lỗi: Dữ liệu đầu vào không hợp lệ");
        return res.status(200).json({ success: false, message: "Dữ liệu không hợp lệ hoặc không có sản phẩm được chọn" });
    }

    const connection = await db.getConnection();
    await connection.beginTransaction();

    try {
        // 1. Tạo Đơn hàng
        const [orderResult] = await connection.query(
            "INSERT INTO orders (user_id, total_amount, payment_method, recipient_name, recipient_phone, recipient_address, note, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            [user_id, total_amount, payment_method, recipient_name, recipient_phone, recipient_address, note, 'pending']
        );
        const orderId = orderResult.insertId;
        console.log(">>> [ORDER CHECKOUT] Đã tạo đơn hàng ID:", orderId);

        // 2. Chốt chi tiết đơn hàng
        const [itemsToOrder] = await connection.query(`
            SELECT c.perfume_id, c.quantity, p.price, p.name, p.stock_quantity
            FROM cart c
            JOIN perfumes p ON c.perfume_id = p.perfume_id
            WHERE c.cart_id IN (?)
        `, [selected_cart_ids]);

        if (itemsToOrder.length === 0) {
            throw new Error("Không tìm thấy sản phẩm hợp lệ trong giỏ hàng");
        }

        for (let item of itemsToOrder) {
            // Kiểm tra tồn kho
            if (item.stock_quantity < item.quantity) {
                throw new Error(`Sản phẩm ${item.name} không đủ số lượng trong kho`);
            }

            // Lưu vào orderdetails
            await connection.query(
                "INSERT INTO orderdetails (order_id, perfume_id, quantity, unit_price) VALUES (?, ?, ?, ?)",
                [orderId, item.perfume_id, item.quantity, item.price]
            );

            // Cập nhật kho
            await connection.query(
                "UPDATE perfumes SET stock_quantity = stock_quantity - ? WHERE perfume_id = ?",
                [item.quantity, item.perfume_id]
            );
            console.log(`>>> [ORDER CHECKOUT] Đã trừ kho sản phẩm ID: ${item.perfume_id}`);
        }

        // 3. Xóa các sản phẩm đã thanh toán khỏi cart
        const [delResult] = await connection.query("DELETE FROM cart WHERE cart_id IN (?)", [selected_cart_ids]);
        console.log(">>> [ORDER CHECKOUT] Đã xóa giỏ hàng, số dòng:", delResult.affectedRows);

        await connection.commit();
        console.log(">>> [ORDER CHECKOUT] HOÀN TẤT TRANSACTION CHO ĐƠN HÀNG:", orderId);

        res.status(200).json({
            success: true,
            order_id: orderId,
            message: "Đặt hàng thành công!"
        });

    } catch (err) {
        await connection.rollback();
        console.error(">>> [ORDER CHECKOUT] LỖI VÀ ĐÃ ROLLBACK:", err.message);
        res.status(200).json({ success: false, message: "Lỗi Server: " + err.message });
    } finally {
        connection.release();
    }
};

exports.getUserOrders = async (req, res) => {
    try {
        const userId = req.params.userId;
        const { status } = req.query;

        let query = "SELECT * FROM orders WHERE user_id = ?";
        let params = [userId];

        if (status && status !== "All") {
            query += " AND status = ?";
            // Chuẩn hóa status khớp với DB
            let dbStatus = status.toLowerCase();
            if (dbStatus === 'processing') dbStatus = 'confirmed';
            params.push(dbStatus);
        }

        query += " ORDER BY order_date DESC";

        const [orders] = await db.query(query, params);
        for (let order of orders) {
            const [details] = await db.query(`
                SELECT od.*, p.name, p.image_url, p.brand,
                (SELECT COUNT(*) FROM reviews r WHERE r.order_id = od.order_id AND r.perfume_id = od.perfume_id) as is_reviewed
                FROM orderdetails od
                JOIN perfumes p ON od.perfume_id = p.perfume_id
                WHERE od.order_id = ?
            `, [order.order_id]);
            order.items = details;
        }
        res.json(orders);
    } catch (err) {
        console.error("Error in getUserOrders:", err);
        res.status(500).json({ error: err.message });
    }
};

exports.getAllOrders = async (req, res) => {
    try {
        const [orders] = await db.query("SELECT o.*, u.full_name FROM orders o JOIN users u ON o.user_id = u.user_id ORDER BY o.order_date DESC");
        for (let order of orders) {
            const [details] = await db.query(`
                SELECT od.*, p.name, p.image_url, p.brand,
                (SELECT COUNT(*) FROM reviews r WHERE r.order_id = od.order_id AND r.perfume_id = od.perfume_id) as is_reviewed
                FROM orderdetails od
                JOIN perfumes p ON od.perfume_id = p.perfume_id
                WHERE od.order_id = ?
            `, [order.order_id]);
            order.items = details;
        }
        res.json(orders);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.updateOrderStatus = async (req, res) => {
    let { order_id, status } = req.body;
    status = status.toLowerCase();
    if (status === 'processing') status = 'confirmed';

    try {
        await db.query("UPDATE orders SET status = ? WHERE order_id = ?", [status, order_id]);
        res.json({ success: true, message: "Cập nhật thành công" });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.getRevenue = async (req, res) => {
    try {
        const { month } = req.query; // format: YYYY-MM

        const [monthlyStats] = await db.query(`
            SELECT
                DATE_FORMAT(o.order_date, '%Y-%m') as month,
                SUM(od.quantity * od.unit_price) as monthly_revenue,
                COUNT(DISTINCT o.order_id) as order_count
            FROM orders o
            JOIN orderdetails od ON o.order_id = od.order_id
            WHERE LOWER(o.status) = 'completed'
            GROUP BY month
            ORDER BY month DESC
        `);

        let ordersDetail = [];
        if (month) {
            const [rows] = await db.query(`
                SELECT o.*, u.full_name
                FROM orders o
                JOIN users u ON o.user_id = u.user_id
                WHERE DATE_FORMAT(o.order_date, '%Y-%m') = ?
                AND LOWER(o.status) = 'completed'
                ORDER BY o.order_date DESC
            `, [month]);

            for (let order of rows) {
                const [details] = await db.query(`
                    SELECT od.*, p.name, p.image_url, p.brand
                    FROM orderdetails od
                    JOIN perfumes p ON od.perfume_id = p.perfume_id
                    WHERE od.order_id = ?
                `, [order.order_id]);

                order.items = details || [];
                order.total_amount = (details || []).reduce((sum, item) => sum + (parseFloat(item.quantity || 0) * parseFloat(item.unit_price || 0)), 0);
            }
            ordersDetail = rows;
        }

        let displayRevenue = 0;
        let displayOrderCount = 0;

        if (month) {
            displayOrderCount = ordersDetail.length;
            displayRevenue = ordersDetail.reduce((sum, order) => sum + parseFloat(order.total_amount || 0), 0);
        } else {
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
