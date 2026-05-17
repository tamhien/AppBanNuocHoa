const mysql = require('mysql2');
require('dotenv').config();

const pool = mysql.createPool({
    host: process.env.DB_HOST || '127.0.0.1',
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASS || '',
    database: process.env.DB_NAME || 'perfumeshop',
    charset: 'utf8mb4',
    waitForConnections: true,
    connectionLimit: 10
});

const promisePool = pool.promise();

/**
 * Khởi tạo Database với hỗ trợ Tiếng Việt (utf8mb4)
 * Đảm bảo tên bảng viết thường để khớp với code xử lý logic
 */
const initDB = async () => {
    try {
        // Đảm bảo Database sử dụng đúng bảng mã
        await promisePool.query("ALTER DATABASE perfumeshop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");

        // Bảng Favorites
        await promisePool.query(`
            CREATE TABLE IF NOT EXISTS favorites (
                favorite_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                perfume_id INT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY user_perfume (user_id, perfume_id)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        `);

        // Bảng VerificationCodes
        await promisePool.query(`
            CREATE TABLE IF NOT EXISTS verificationcodes (
                id INT AUTO_INCREMENT PRIMARY KEY,
                email VARCHAR(255) NOT NULL,
                code VARCHAR(6) NOT NULL,
                expires_at TIMESTAMP NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        `);

        // Bảng Cart
        await promisePool.query(`
            CREATE TABLE IF NOT EXISTS cart (
                cart_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                perfume_id INT NOT NULL,
                quantity INT NOT NULL DEFAULT 1,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        `);

        // Bảng Orders
        await promisePool.query(`
            CREATE TABLE IF NOT EXISTS orders (
                order_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                total_amount DECIMAL(10,2) NOT NULL,
                status ENUM('pending', 'confirmed', 'shipping', 'completed', 'cancelled') DEFAULT 'pending',
                payment_method VARCHAR(50) NOT NULL,
                recipient_name VARCHAR(255) NOT NULL,
                recipient_phone VARCHAR(20) NOT NULL,
                recipient_address TEXT NOT NULL,
                note TEXT,
                order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        `);

        // Bảng OrderDetails
        await promisePool.query(`
            CREATE TABLE IF NOT EXISTS orderdetails (
                order_detail_id INT AUTO_INCREMENT PRIMARY KEY,
                order_id INT NOT NULL,
                perfume_id INT NOT NULL,
                quantity INT NOT NULL,
                unit_price DECIMAL(10,2) NOT NULL
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        `);

        // Bảng Reviews (đã thêm từ trước, đảm bảo collation)
        await promisePool.query(`
            CREATE TABLE IF NOT EXISTS reviews (
                review_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                perfume_id INT NOT NULL,
                order_id INT NOT NULL,
                rating INT NOT NULL,
                comment TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        `);

        console.log(">>> [DATABASE] Khởi tạo thành công (UTF-8 Ready)");
    } catch (err) {
        console.error(">>> [DATABASE] Lỗi khởi tạo:", err.message);
    }
};

initDB();

module.exports = promisePool;
