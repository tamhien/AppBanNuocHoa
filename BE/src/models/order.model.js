const db = require('../config/db');

const Order = {
    updateStatus: async (connection, orderId, status) => {
        const query = "UPDATE orders SET status = ? WHERE order_id = ?";
        return await connection.query(query, [status, orderId]);
    },

    findById: async (orderId) => {
        const [rows] = await db.query("SELECT * FROM orders WHERE order_id = ?", [orderId]);
        return rows[0];
    },

    create: async (connection, data) => {
        const { user_id, total_amount, payment_method, recipient_name, recipient_phone, recipient_address, note } = data;
        const [result] = await connection.query(
            "INSERT INTO orders (user_id, total_amount, payment_method, recipient_name, recipient_phone, recipient_address, note) VALUES (?, ?, ?, ?, ?, ?, ?)",
            [user_id, total_amount, payment_method, recipient_name, recipient_phone, recipient_address, note]
        );
        return result.insertId;
    }
};

module.exports = Order;
