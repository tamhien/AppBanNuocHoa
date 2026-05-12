const db = require('../config/db');

// Thêm sản phẩm vào giỏ hàng
exports.addToCart = async (req, res) => {
    const { user_id, perfume_id, quantity } = req.body;
    console.log("AddToCart Request:", { user_id, perfume_id, quantity });
    try {
        if (!user_id || !perfume_id) {
            return res.status(400).json({ success: false, message: "Thiếu thông tin User hoặc Sản phẩm" });
        }

        // Kiểm tra xem sản phẩm đã có trong giỏ hàng chưa
        const [existing] = await db.query(
            "SELECT * FROM Cart WHERE user_id = ? AND perfume_id = ?",
            [user_id, perfume_id]
        );

        if (existing.length > 0) {
            // Nếu có rồi thì cập nhật số lượng
            const newQuantity = parseInt(existing[0].quantity) + parseInt(quantity || 1);
            await db.query(
                "UPDATE Cart SET quantity = ? WHERE cart_id = ?",
                [newQuantity, existing[0].cart_id]
            );
            res.json({ success: true, message: "Đã cập nhật số lượng trong giỏ hàng" });
        } else {
            // Nếu chưa có thì thêm mới
            await db.query(
                "INSERT INTO Cart (user_id, perfume_id, quantity) VALUES (?, ?, ?)",
                [user_id, perfume_id, quantity || 1]
            );
            res.json({ success: true, message: "Đã thêm vào giỏ hàng" });
        }
    } catch (err) {
        console.error("Lỗi addToCart:", err.message);
        res.status(500).json({ success: false, error: err.message });
    }
};

// Lấy danh sách giỏ hàng của user
exports.getCartByUserId = async (req, res) => {
    const { userId } = req.params;
    try {
        const [rows] = await db.query(`
            SELECT c.*, p.name, p.price, p.image_url, p.brand
            FROM Cart c
            JOIN Perfumes p ON c.perfume_id = p.perfume_id
            WHERE c.user_id = ?
        `, [userId]);
        res.json(rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

// Cập nhật số lượng trong giỏ hàng
exports.updateCartQuantity = async (req, res) => {
    const { cart_id, quantity } = req.body;
    try {
        if (quantity <= 0) {
            await db.query("DELETE FROM Cart WHERE cart_id = ?", [cart_id]);
            res.json({ success: true, message: "Đã xóa sản phẩm khỏi giỏ hàng" });
        } else {
            await db.query("UPDATE Cart SET quantity = ? WHERE cart_id = ?", [quantity, cart_id]);
            res.json({ success: true, message: "Đã cập nhật số lượng" });
        }
    } catch (err) {
        res.status(500).json({ success: false, error: err.message });
    }
};

// Xóa sản phẩm khỏi giỏ hàng
exports.deleteCartItem = async (req, res) => {
    const { cartId } = req.params;
    try {
        await db.query("DELETE FROM Cart WHERE cart_id = ?", [cartId]);
        res.json({ success: true, message: "Đã xóa sản phẩm khỏi giỏ hàng" });
    } catch (err) {
        res.status(500).json({ success: false, error: err.message });
    }
};
