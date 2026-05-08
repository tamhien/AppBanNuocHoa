require('dotenv').config();
const express = require('express');
const cors = require('cors');
const path = require('path');
const apiRoutes = require('./routes/api');

const app = express();
app.use(cors());
app.use(express.json());

// Cấu hình folder uploads tuyệt đối
app.use('/uploads', express.static(path.join(__dirname, '../uploads')));

app.use('/api', apiRoutes);

// Middleware bắt lỗi tập trung (giúp xử lý lỗi 500)
app.use((err, req, res, next) => {
    console.error("LỖI HỆ THỐNG:", err.stack);
    res.status(500).json({
        success: false,
        message: "Lỗi Server nội bộ",
        error: err.message
    });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(`=============================================`);
    console.log(`Server đang chạy tại: http://localhost:${PORT}`);
    console.log(`Thư mục ảnh: ${path.join(__dirname, '../uploads')}`);
    console.log(`=============================================`);
});
