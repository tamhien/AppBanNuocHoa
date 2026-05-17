const { Payment, Order } = require('../models');
const { sortObject } = require('../utils/vnpay');
const moment = require('moment');
const crypto = require('crypto');
const queryString = require('qs');
const db = require('../config/db');

const createVnpayUrl = async (req, data) => {
    const { order_id, amount, bankCode } = data;
    const tmnCode = process.env.VNP_TMN_CODE;
    const secretKey = process.env.VNP_HASH_SECRET;
    let vnpUrl = process.env.VNP_URL;
    const returnUrl = process.env.VNP_RETURN_URL;

    if (!tmnCode || !secretKey || !vnpUrl || !returnUrl) {
        throw new Error("Thiếu cấu hình VNPay trong .env");
    }

    const date = new Date();
    const createDate = moment(date).format('YYYYMMDDHHmmss');

    let ipAddr = req.headers['x-forwarded-for'] || req.connection?.remoteAddress || '1.1.1.1';
    if (ipAddr.includes('::ffff:')) ipAddr = ipAddr.replace('::ffff:', '');
    if (ipAddr === '::1' || ipAddr === '127.0.0.1') ipAddr = '1.1.1.1';

    let vnp_Params = {};
    vnp_Params['vnp_Version'] = '2.1.0';
    vnp_Params['vnp_Command'] = 'pay';
    vnp_Params['vnp_TmnCode'] = tmnCode.trim();
    vnp_Params['vnp_Locale'] = 'vn';
    vnp_Params['vnp_CurrCode'] = 'VND';
    vnp_Params['vnp_TxnRef'] = order_id.toString() + '_' + createDate;
    vnp_Params['vnp_OrderInfo'] = 'Thanh toan don hang ' + order_id;
    vnp_Params['vnp_OrderType'] = 'other';
    vnp_Params['vnp_Amount'] = Math.floor(parseFloat(amount) * 25000) * 100;
    vnp_Params['vnp_ReturnUrl'] = returnUrl.trim();
    vnp_Params['vnp_IpAddr'] = ipAddr;
    vnp_Params['vnp_CreateDate'] = createDate;

    if (bankCode) vnp_Params['vnp_BankCode'] = bankCode;

    vnp_Params = sortObject(vnp_Params);
    const signData = queryString.stringify(vnp_Params, { encode: false });
    const hmac = crypto.createHmac("sha512", secretKey.trim());
    const signed = hmac.update(Buffer.from(signData, 'utf-8')).digest("hex");

    vnp_Params['vnp_SecureHash'] = signed;
    return vnpUrl + '?' + queryString.stringify(vnp_Params, { encode: false });
};

const processVnpayTransaction = async (vnp_Params) => {
    const responseCode = vnp_Params['vnp_ResponseCode'];
    const secureHash = vnp_Params['vnp_SecureHash'];
    const secretKey = process.env.VNP_HASH_SECRET;

    delete vnp_Params['vnp_SecureHash'];
    delete vnp_Params['vnp_SecureHashType'];

    const sortedParams = sortObject(vnp_Params);
    const signData = queryString.stringify(sortedParams, { encode: false });
    const hmac = crypto.createHmac("sha512", secretKey.trim());
    const signed = hmac.update(Buffer.from(signData, 'utf-8')).digest("hex");

    if (secureHash !== signed) {
        throw new Error("Chữ ký không hợp lệ");
    }

    const txnRef = vnp_Params['vnp_TxnRef'];
    const order_id = txnRef.split('_')[0];
    const vnp_TransactionNo = vnp_Params['vnp_TransactionNo'];

    if (responseCode === "00") {
        const connection = await db.getConnection();
        try {
            await connection.beginTransaction();

            await Payment.create(connection, {
                order_id: order_id,
                amount_paid: vnp_Params['vnp_Amount'] / 100,
                payment_method: 'VNPAY',
                vnp_transaction_no: vnp_TransactionNo,
                vnp_response_code: responseCode
            });

            await Order.updateStatus(connection, order_id, 'confirmed');

            await connection.commit();
            return { success: true, message: 'Thanh toán thành công' };
        } catch (error) {
            await connection.rollback();
            throw error;
        } finally {
            connection.release();
        }
    } else {
        return { success: false, message: 'Giao dịch thất bại: ' + responseCode };
    }
};

module.exports = {
    createVnpayUrl,
    processVnpayTransaction
};
