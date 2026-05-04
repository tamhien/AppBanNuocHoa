const db = require('../config/db');

// Lấy danh sách (có thể lọc theo gender)
exports.getAllPerfumes = async (req, res) => {
    const { gender } = req.query; // Ví dụ: ?gender=Men
    let query = "SELECT * FROM Perfumes";
    let params = [];
    if (gender) {
        query += " WHERE gender = ?";
        params.push(gender);
    }
    try {
        const [rows] = await db.query(query, params);
        res.json(rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// Xem chi tiết
exports.getPerfumeById = async (req, res) => {
    try {
        const [rows] = await db.query("SELECT * FROM Perfumes WHERE perfume_id = ?", [req.params.id]);
        res.json(rows[0]);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// Admin: Thêm hàng mới
exports.addPerfume = async (req, res) => {
    const { name, brand, description, price, stock_quantity, image_url, gender } = req.body;
    try {
        await db.query(
            "INSERT INTO Perfumes (name, brand, description, price, stock_quantity, image_url, gender) VALUES (?, ?, ?, ?, ?, ?, ?)",
            [name, brand, description, price, stock_quantity, image_url, gender]
        );
        res.json({ success: true, message: "Thêm thành công" });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};