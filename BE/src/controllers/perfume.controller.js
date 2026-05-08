const db = require('../config/db');

// Lấy tất cả sản phẩm
exports.getAllPerfumes = async (req, res) => {
    const { gender } = req.query;
    let query = `
        SELECT p.*, COALESCE(SUM(od.quantity), 0) as sold_count
        FROM Perfumes p
        LEFT JOIN OrderDetails od ON p.perfume_id = od.perfume_id
        GROUP BY p.perfume_id
    `;
    let params = [];
    if (gender) {
        query = `
            SELECT p.*, COALESCE(SUM(od.quantity), 0) as sold_count
            FROM Perfumes p
            LEFT JOIN OrderDetails od ON p.perfume_id = od.perfume_id
            WHERE p.gender = ?
            GROUP BY p.perfume_id
        `;
        params.push(gender);
    }
    try {
        const [rows] = await db.query(query, params);
        res.json(rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// Xem chi tiết sản phẩm
exports.getPerfumeById = async (req, res) => {
    try {
        const [rows] = await db.query("SELECT * FROM Perfumes WHERE perfume_id = ?", [req.params.id]);
        if (rows.length > 0) {
            res.json(rows[0]);
        } else {
            res.status(404).json({ message: "Không tìm thấy sản phẩm" });
        }
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// Admin: Thêm sản phẩm mới
exports.addPerfume = async (req, res) => {
    const { name, brand, description, price, stock_quantity, image_url, gender } = req.body;
    try {
        await db.query(
            "INSERT INTO Perfumes (name, brand, description, price, stock_quantity, image_url, gender) VALUES (?, ?, ?, ?, ?, ?, ?)",
            [name, brand, description, price, stock_quantity, image_url, gender]
        );
        res.json({ success: true, message: "Thêm sản phẩm thành công" });
    } catch (err) {
        console.error("Lỗi thêm SP:", err);
        res.status(500).json({ success: false, error: err.message });
    }
};

// Admin: Cập nhật sản phẩm
exports.updatePerfume = async (req, res) => {
    const { name, brand, description, price, stock_quantity, image_url, gender } = req.body;
    try {
        const [result] = await db.query(
            "UPDATE Perfumes SET name=?, brand=?, description=?, price=?, stock_quantity=?, image_url=?, gender=? WHERE perfume_id=?",
            [name, brand, description, price, stock_quantity, image_url, gender, req.params.id]
        );
        res.json({ success: true, message: "Cập nhật thành công" });
    } catch (err) {
        console.error("Lỗi cập nhật SP:", err);
        res.status(500).json({ success: false, error: err.message });
    }
};

// Admin: Xóa sản phẩm
exports.deletePerfume = async (req, res) => {
    try {
        await db.query("DELETE FROM Perfumes WHERE perfume_id = ?", [req.params.id]);
        res.json({ success: true, message: "Xóa thành công" });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
