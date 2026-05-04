const db = require('../config/db');

exports.register = async (req, res) => {
    const { username, password, full_name, email, phone, address } = req.body;
    try {
        await db.query(
            "INSERT INTO Users (username, password_hash, full_name, email, phone, address, role) VALUES (?, ?, ?, ?, ?, ?, 'user')",
            [username, password, full_name, email, phone, address]
        );
        res.status(201).json({ success: true, message: "Đăng ký thành công!" });
    } catch (err) {
        res.status(500).json({ success: false, message: err.message });
    }
};

exports.login = async (req, res) => {
    const { username, password } = req.body;
    try {
        const [rows] = await db.query("SELECT * FROM Users WHERE username = ? AND password_hash = ?", [username, password]);
        if (rows.length > 0) {
            res.json({
                success: true,
                user_id: rows[0].user_id,
                role: rows[0].role, // 'admin' hoặc 'user'
                full_name: rows[0].full_name
            });
        } else {
            res.status(401).json({ success: false, message: "Sai tài khoản hoặc mật khẩu" });
        }
    } catch (err) {
        res.status(500).json({ success: false, message: err.message });
    }
};