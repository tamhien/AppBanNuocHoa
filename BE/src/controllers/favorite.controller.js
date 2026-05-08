const db = require('../config/db');

// Thêm vào yêu thích
exports.addFavorite = async (req, res) => {
    const { user_id, perfume_id } = req.body;
    console.log("Adding favorite:", { user_id, perfume_id });
    try {
        // Sử dụng INSERT IGNORE để tránh lỗi nếu đã tồn tại
        await db.query("INSERT IGNORE INTO Favorites (user_id, perfume_id) VALUES (?, ?)", [user_id, perfume_id]);
        res.json({ success: true, message: "Đã thêm vào danh sách yêu thích" });
    } catch (err) {
        console.error("Lỗi addFavorite:", err);
        res.status(500).json({ error: err.message });
    }
};

// Lấy danh sách yêu thích của User
exports.getFavorites = async (req, res) => {
    const { userId } = req.params;
    try {
        const query = `
            SELECT p.*, COALESCE((SELECT SUM(quantity) FROM OrderDetails WHERE perfume_id = p.perfume_id), 0) as sold_count
            FROM Favorites f
            JOIN Perfumes p ON f.perfume_id = p.perfume_id
            WHERE f.user_id = ?
        `;
        const [rows] = await db.query(query, [userId]);
        res.json(rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// Xóa khỏi yêu thích
exports.removeFavorite = async (req, res) => {
    const { userId, perfumeId } = req.params;
    try {
        await db.query("DELETE FROM Favorites WHERE user_id = ? AND perfume_id = ?", [userId, perfumeId]);
        res.json({ success: true, message: "Đã xóa khỏi danh sách yêu thích" });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
