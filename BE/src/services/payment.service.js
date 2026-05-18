const db = require('../config/db');

async function createPayment(payload) {
  const { order_id, amount_paid, payment_method } = payload;
  const numericOrderId = parseInt(order_id);

  console.log(`\n[VNPAY LOG] >>> Bắt đầu xử lý thanh toán cho Đơn hàng: ${numericOrderId}`);
  const connection = await db.getConnection();

  try {
    await connection.beginTransaction();

    // Idempotency: Kiểm tra nếu đã thanh toán rồi thì không chèn thêm
    const [existing] = await connection.query(
        "SELECT * FROM payments WHERE order_id = ? AND payment_method = ?",
        [numericOrderId, payment_method]
    );

    if (existing.length === 0) {
        await connection.query(
            "INSERT INTO payments (order_id, amount_paid, payment_method) VALUES (?, ?, ?)",
            [numericOrderId, amount_paid, payment_method]
        );
    }

    // Cập nhật trạng thái đơn hàng sang 'confirmed'
    await connection.query(
        "UPDATE orders SET status = 'confirmed' WHERE order_id = ?",
        [numericOrderId]
    );

    await connection.commit();
    console.log(`[VNPAY SUCCESS] - Đã xác nhận thanh toán đơn hàng ${numericOrderId}\n`);
    return { success: true };

  } catch (error) {
    await connection.rollback();
    console.error(`[VNPAY ERROR] - Lỗi xử lý: ${error.message}`);
    throw error;
  } finally {
    connection.release();
  }
}

module.exports = { createPayment };
