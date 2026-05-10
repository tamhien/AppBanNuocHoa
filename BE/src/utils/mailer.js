const nodemailer = require('nodemailer');
require('dotenv').config();

const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: process.env.EMAIL_USER,
        pass: process.env.EMAIL_PASS
    }
});

exports.sendOTP = async (email, code) => {
    const mailOptions = {
        from: `"PrincePhom Shop" <${process.env.EMAIL_USER}>`,
        to: email,
        subject: 'Mã xác nhận đổi mật khẩu - PrincePhom Shop',
        text: `Mã xác nhận của bạn là: ${code}. Mã này có hiệu lực trong 5 phút.`
    };

    try {
        await transporter.sendMail(mailOptions);
        return true;
    } catch (error) {
        console.error('Lỗi gửi mail:', error);
        return false;
    }
};
