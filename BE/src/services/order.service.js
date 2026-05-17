const db = require('../config/db');
const { Order, Cart, Perfume } = require('../models');

const checkout = async (data) => {
    const { user_id, selected_cart_ids, total_amount, payment_method, recipient_name, recipient_phone, recipient_address, note } = data;

    const connection = await db.getConnection();
    await connection.beginTransaction();

    try {
        // 1. Tạo Đơn hàng
        const orderId = await Order.create(connection, {
            user_id, total_amount, payment_method, recipient_name, recipient_phone, recipient_address, note
        });

        // 2. Chốt chi tiết đơn hàng
        const itemsToOrder = await Cart.getItemsToOrder(connection, selected_cart_ids);

        if (itemsToOrder.length === 0) {
            throw new Error("Không tìm thấy sản phẩm hợp lệ trong giỏ hàng");
        }

        for (let item of itemsToOrder) {
            const perfume = await Perfume.checkStock(connection, item.perfume_id);
            if (!perfume || perfume.stock_quantity < item.quantity) {
                throw new Error(`Sản phẩm ${perfume?.name || ''} không đủ số lượng trong kho`);
            }

            // Thêm chi tiết đơn hàng
            await connection.query(
                "INSERT INTO orderdetails (order_id, perfume_id, quantity, unit_price) VALUES (?, ?, ?, ?)",
                [orderId, item.perfume_id, item.quantity, item.price]
            );

            // Cập nhật kho
            await Perfume.updateStock(connection, item.perfume_id, item.quantity);
        }

        // 3. Xóa giỏ hàng
        await Cart.deleteItems(connection, selected_cart_ids);

        await connection.commit();
        return { success: true, orderId };
    } catch (err) {
        await connection.rollback();
        throw err;
    } finally {
        connection.release();
    }
};

module.exports = {
    checkout
};
