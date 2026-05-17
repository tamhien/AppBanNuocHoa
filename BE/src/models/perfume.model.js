const db = require('../config/db');

const Perfume = {
    checkStock: async (connection, perfumeId) => {
        const [rows] = await connection.query("SELECT stock_quantity, name FROM perfumes WHERE perfume_id = ?", [perfumeId]);
        return rows[0];
    },
    updateStock: async (connection, perfumeId, quantity) => {
        return await connection.query(
            "UPDATE perfumes SET stock_quantity = stock_quantity - ? WHERE perfume_id = ?",
            [quantity, perfumeId]
        );
    }
};

module.exports = Perfume;
