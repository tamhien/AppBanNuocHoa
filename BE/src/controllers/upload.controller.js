const multer = require('multer');
const path = require('path');
const fs = require('fs');

// Đường dẫn tuyệt đối đến thư mục uploads ở gốc project BE
const uploadDir = path.join(__dirname, '../../uploads');

// Tự động tạo thư mục nếu chưa có
if (!fs.existsSync(uploadDir)) {
    fs.mkdirSync(uploadDir, { recursive: true });
}

const storage = multer.diskStorage({
    destination: (req, file, cb) => {
        cb(null, uploadDir);
    },
    filename: (req, file, cb) => {
        // Tên file: thời gian_ngẫu nhiên.đuôi_mở_rộng
        const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1E9);
        cb(null, uniqueSuffix + path.extname(file.originalname));
    }
});

const upload = multer({
    storage: storage,
    limits: { fileSize: 5 * 1024 * 1024 } // Giới hạn 5MB
});

exports.uploadImage = [
    upload.single('image'),
    (req, res) => {
        if (!req.file) {
            return res.status(400).json({ success: false, message: "Không có file" });
        }
        // Trả về tên file để lưu vào Database
        res.json({
            success: true,
            imageUrl: `/uploads/${req.file.filename}`,
            fileName: req.file.filename
        });
    }
];
