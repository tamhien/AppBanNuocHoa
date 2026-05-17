const db = require('../config/db');

const Cart = {
    getItemsToOrder: async (connection, selectedCartIds) => {
        const [rows] = await connection.query(`
            SELECT c.perfume_id, c.quantity, p.price
            FROM cart c
            JOIN perfumes p ON c.perfume_id = p.perfume_id
            WHERE c.cart_id IN (?)
        `, [selectedCartIds]);
        return rows;
    },
    deleteItems: async (connection, cartIds) => {
        return await connection.query("DELETE FROM cart WHERE cart_id IN (?)", [cartIds]);
    }
};

module.exports = Cart;
