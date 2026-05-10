const db = require('../config/db');
const mailer = require('../utils/mailer');

exports.register = async (req, res) => {
    const { username, password, full_name, email, phone, address } = req.body;
    try {
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

// Lấy thông tin cá nhân
exports.getProfile = async (req, res) => {
    console.log("Fetching profile for user ID:", req.params.id);
    try {
        const [rows] = await db.query(
            "SELECT user_id, username, full_name, email, phone, address, role FROM Users WHERE user_id = ?",
            [req.params.id]
        );
        if (rows.length > 0) {
            console.log("Profile found:", rows[0]);
            res.json(rows[0]);
        } else {
            console.log("Profile not found for ID:", req.params.id);
            res.status(404).json({ success: false, message: "Không tìm thấy người dùng" });
        }
    } catch (err) {
        console.error("Lỗi getProfile SQL:", err.message);
        res.status(500).json({ success: false, error: err.message });
    }
};

// Cập nhật thông tin cá nhân
exports.updateProfile = async (req, res) => {
    const { full_name, email, phone, address } = req.body;
    console.log("Updating profile for user ID:", req.params.id, "Data:", { full_name, email, phone, address });
    try {
        await db.query(
            "UPDATE Users SET full_name = ?, email = ?, phone = ?, address = ? WHERE user_id = ?",
            [full_name, email, phone, address, req.params.id]
        );
        res.json({ success: true, message: "Cập nhật thông tin thành công" });
    } catch (err) {
        console.error("Lỗi updateProfile SQL:", err.message);
        res.status(500).json({ success: false, error: err.message });
    }
};

// Đổi mật khẩu
exports.changePassword = async (req, res) => {
    const { current_password, new_password } = req.body;
    try {
        const [rows] = await db.query(
            "SELECT * FROM Users WHERE user_id = ? AND password_hash = ?",
            [req.params.id, current_password]
        );
        if (rows.length > 0) {
            await db.query(
                "UPDATE Users SET password_hash = ? WHERE user_id = ?",
                [new_password, req.params.id]
            );
            res.json({ success: true, message: "Đổi mật khẩu thành công" });
        } else {
            res.status(400).json({ success: false, message: "Mật khẩu hiện tại không đúng" });
        }
    } catch (err) {
        res.status(500).json({ success: false, error: err.message });
    }
};

// Gửi mã OTP về mail
exports.sendOTP = async (req, res) => {
    const { email } = req.body;
    const otp = Math.floor(100000 + Math.random() * 900000).toString(); // Tạo mã 6 số
    const expiresAt = new Date(Date.now() + 5 * 60 * 1000); // Hết hạn sau 5 phút

    try {
        // Lưu mã vào DB
        await db.query(
            "INSERT INTO VerificationCodes (email, code, expires_at) VALUES (?, ?, ?)",
            [email, otp, expiresAt]
        );

        const sent = await mailer.sendOTP(email, otp);
        if (sent) {
            res.json({ success: true, message: "Mã OTP đã được gửi về email của bạn" });
        } else {
            res.status(500).json({ success: false, message: "Không thể gửi email. Hãy kiểm tra EMAIL_USER và EMAIL_PASS trong .env" });
        }
    } catch (err) {
        res.status(500).json({ success: false, error: err.message });
    }
};

// Xác nhận OTP và đổi mật khẩu mới
exports.resetPasswordWithOTP = async (req, res) => {
    const { email, otp, newPassword } = req.body;
    try {
        // Kiểm tra mã OTP
        const [rows] = await db.query(
            "SELECT * FROM VerificationCodes WHERE email = ? AND code = ? AND expires_at > NOW() ORDER BY created_at DESC LIMIT 1",
            [email, otp]
        );

        if (rows.length > 0) {
            // Cập nhật mật khẩu cho user có email này
            const [userRows] = await db.query("SELECT user_id FROM Users WHERE email = ?", [email]);
            if (userRows.length > 0) {
                await db.query("UPDATE Users SET password_hash = ? WHERE email = ?", [newPassword, email]);
                // Xóa mã đã dùng
                await db.query("DELETE FROM VerificationCodes WHERE email = ?", [email]);
                res.json({ success: true, message: "Đổi mật khẩu thành công" });
            } else {
                res.status(404).json({ success: false, message: "Không tìm thấy người dùng với email này" });
            }
        } else {
            res.status(400).json({ success: false, message: "Mã OTP không đúng hoặc đã hết hạn" });
        }
    } catch (err) {
        res.status(500).json({ success: false, error: err.message });
    }
};

// Quên mật khẩu
exports.forgotPassword = async (req, res) => {
    const { username, email } = req.body;
    try {
        const [rows] = await db.query(
            "SELECT * FROM Users WHERE username = ? AND email = ?",
            [username, email]
        );
        if (rows.length > 0) {
            const defaultPasswordHash = '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92';
            await db.query("UPDATE Users SET password_hash = ? WHERE user_id = ?", [defaultPasswordHash, rows[0].user_id]);
            res.json({ success: true, message: "Mật khẩu đã được reset về '123456'" });
        } else {
            res.status(404).json({ success: false, message: "Thông tin không khớp" });
        }
    } catch (err) {
        res.status(500).json({ success: false, error: err.message });
    }
};
