const db = require('../config/db');

exports.addReview = async (req, res) => {
    const { user_id, perfume_id, order_id, rating, comment } = req.body;
    try {
        // Sử dụng tên bảng 'reviews' viết thường
        const [existing] = await db.query(
            "SELECT * FROM reviews WHERE order_id = ? AND perfume_id = ?",
            [order_id, perfume_id]
        );

        if (existing.length > 0) {
            return res.status(400).json({ success: false, message: "Bạn đã đánh giá sản phẩm này cho đơn hàng này rồi" });
        }

        await db.query(
            "INSERT INTO reviews (user_id, perfume_id, order_id, rating, comment) VALUES (?, ?, ?, ?, ?)",
            [user_id, perfume_id, order_id, rating, comment]
        );
        res.json({ success: true, message: "Đánh giá thành công!" });
    } catch (err) {
        console.error("Error in addReview:", err);
        res.status(500).json({ success: false, error: err.message });
    }
};

exports.getPerfumeReviews = async (req, res) => {
    const { perfumeId } = req.params;
    try {
        const [rows] = await db.query(`
            SELECT r.*, u.full_name
            FROM reviews r
            JOIN users u ON r.user_id = u.user_id
            WHERE r.perfume_id = ?
            ORDER BY r.created_at DESC
        `, [perfumeId]);
        res.json(rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};
