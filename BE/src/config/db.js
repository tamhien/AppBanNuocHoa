const mysql = require('mysql2');
require('dotenv').config();

const pool = mysql.createPool({
    host: process.env.DB_HOST || '127.0.0.1',
    user: process.env.DB_USER || 'root',
    password: process.env.DB_PASS || '',
    database: process.env.DB_NAME || 'PerfumeShop',
    waitForConnections: true,
    connectionLimit: 10
});

const promisePool = pool.promise();

// Tạo bảng Favorites nếu chưa có
const initDB = async () => {
    try {
        await promisePool.query(`
            CREATE TABLE IF NOT EXISTS Favorites (
                favorite_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                perfume_id INT NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY user_perfume (user_id, perfume_id),
                FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
                FOREIGN KEY (perfume_id) REFERENCES Perfumes(perfume_id) ON DELETE CASCADE
            )
        `);
        // Tạo bảng VerificationCodes
        await promisePool.query(`
            CREATE TABLE IF NOT EXISTS VerificationCodes (
                id INT AUTO_INCREMENT PRIMARY KEY,
                email VARCHAR(255) NOT NULL,
                code VARCHAR(6) NOT NULL,
                expires_at TIMESTAMP NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        `);

        // Tạo bảng Cart
        await promisePool.query(`
            CREATE TABLE IF NOT EXISTS Cart (
                cart_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                perfume_id INT NOT NULL,
                quantity INT NOT NULL DEFAULT 1,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
                FOREIGN KEY (perfume_id) REFERENCES Perfumes(perfume_id) ON DELETE CASCADE
            )
        `);

        // Tạo bảng Orders
        await promisePool.query(`
            CREATE TABLE IF NOT EXISTS Orders (
                order_id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                total_amount DECIMAL(10,2) NOT NULL,
                status ENUM('Pending', 'Processing', 'Shipping', 'Completed', 'Cancelled') DEFAULT 'Pending',
                payment_method VARCHAR(50) NOT NULL,
                recipient_name VARCHAR(255) NOT NULL,
                recipient_phone VARCHAR(20) NOT NULL,
                recipient_address TEXT NOT NULL,
                note TEXT,
                order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
            )
        `);

        // Sửa lỗi thiếu cột nếu bảng đã tồn tại từ trước
        try {
            await promisePool.query("ALTER TABLE Orders ADD COLUMN IF NOT EXISTS payment_method VARCHAR(50) NOT NULL AFTER status");
            await promisePool.query("ALTER TABLE Orders ADD COLUMN IF NOT EXISTS recipient_name VARCHAR(255) NOT NULL AFTER payment_method");
            await promisePool.query("ALTER TABLE Orders ADD COLUMN IF NOT EXISTS recipient_phone VARCHAR(20) NOT NULL AFTER recipient_name");
            await promisePool.query("ALTER TABLE Orders ADD COLUMN IF NOT EXISTS recipient_address TEXT NOT NULL AFTER recipient_phone");
            await promisePool.query("ALTER TABLE Orders ADD COLUMN IF NOT EXISTS note TEXT AFTER recipient_address");
        } catch (alterError) {
            // IF NOT EXISTS có thể không hỗ trợ ở một số phiên bản MariaDB/MySQL cũ,
            // nhưng lỗi này có thể bỏ qua nếu cột đã có.
            console.log("Check/Update columns for Orders table");
        }

        // Tạo bảng OrderDetails
        await promisePool.query(`
            CREATE TABLE IF NOT EXISTS OrderDetails (
                detail_id INT AUTO_INCREMENT PRIMARY KEY,
                order_id INT NOT NULL,
                perfume_id INT NOT NULL,
                quantity INT NOT NULL,
                unit_price DECIMAL(10,2) NOT NULL,
                FOREIGN KEY (order_id) REFERENCES Orders(order_id) ON DELETE CASCADE,
                FOREIGN KEY (perfume_id) REFERENCES Perfumes(perfume_id) ON DELETE CASCADE
            )
        `);

        console.log("Database initialized (Tables checked: Favorites, Codes, Cart, Orders, Details)");
    } catch (err) {
        console.error("Lỗi khởi tạo DB:", err.message);
    }
};

initDB();

module.exports = promisePool;