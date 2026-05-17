const db = require('../config/db');

const Payment = {
    create: async (connection, data) => {
        const { order_id, amount_paid, payment_method, vnp_transaction_no, vnp_response_code } = data;
        const query = "INSERT INTO payments (order_id, amount_paid, payment_method, vnp_transaction_no, vnp_response_code) VALUES (?, ?, ?, ?, ?)";
        return await connection.query(query, [order_id, amount_paid, payment_method, vnp_transaction_no, vnp_response_code]);
    }
};

module.exports = Payment;
