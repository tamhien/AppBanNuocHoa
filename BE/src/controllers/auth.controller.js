const db = require('../config/db');

exports.register = async (req, res) => {
    const { username, password, full_name, email, phone, address } = req.body;
    try {
        // Kiểm tra xem các trường quan trọng có bị undefined không
        if (!username || !password) {
            return res.status(400).json({ success: false, message: "Thiếu tài khoản hoặc mật khẩu" });
        }

        await db.query(
            "INSERT INTO Users (username, password_hash, full_name, email, phone, address, role) VALUES (?, ?, ?, ?, ?, ?, 'user')",
            [username, password, full_name, email, phone, address]
        );
        res.status(201).json({ success: true, message: "Đăng ký thành công!" });
    } catch (err) {
        console.error("LỖI ĐĂNG KÝ SQL:", err.message);
        // Trả về thông báo lỗi chi tiết cho App
        res.status(500).json({
            success: false,
            message: "Lỗi Database: " + err.message
        });
    }
};

exports.getAllUsers = async (req, res) => {
    try {
        const [rows] = await db.query("SELECT user_id, username, full_name, email, phone, address, role FROM Users WHERE role = 'user'");
        res.json(rows);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
};

exports.deleteUser = async (req, res) => {
    try {
        await db.query("DELETE FROM Users WHERE user_id = ?", [req.params.id]);
        res.json({ success: true, message: "Xóa người dùng thành công" });
    } catch (err) {
        res.status(500).json({ error: err.message });
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
                role: rows[0].role,
                full_name: rows[0].full_name
            });
        } else {
            res.status(401).json({ success: false, message: "Sai tài khoản hoặc mật khẩu" });
        }
    } catch (err) {
        console.error("LỖI ĐĂNG NHẬP SQL:", err.message);
        res.status(500).json({
            success: false,
            message: "Lỗi Database: " + err.message
        });
    }
};
