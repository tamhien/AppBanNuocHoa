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
        console.log("Database initialized (Favorites & VerificationCodes tables checked)");
    } catch (err) {
        console.error("Lỗi khởi tạo DB:", err.message);
    }
};

initDB();

module.exports = promisePool;