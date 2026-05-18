const db = require('../config/db');
const { sortObject } = require('../utils/vnpay');
const { createPayment } = require('../services/payment.service');
const moment = require('moment');
const crypto = require('crypto');
const queryString = require('qs');

/**
 * Hàm giải mã các mã lỗi từ VNPay
 */
function getVnpayErrorMessage(code) {
  const errors = {
    '00': 'Giao dịch thành công',
    '07': 'Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường).',
    '09': 'Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng.',
    '10': 'Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần',
    '11': 'Giao dịch không thành công do: Đã hết hạn chờ thanh toán. Xin quý khách vui lòng thực hiện lại giao dịch.',
    '12': 'Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa.',
    '13': 'Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch (OTP). Xin quý khách vui lòng thực hiện lại giao dịch.',
    '24': 'Giao dịch không thành công do: Khách hàng hủy giao dịch',
    '51': 'Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch.',
    '65': 'Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày.',
    '75': 'Ngân hàng thanh toán đang bảo trì.',
    '79': 'Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định. Xin quý khách vui lòng thực hiện lại giao dịch',
    '99': 'Các lỗi khác (lỗi phát sinh tại VNPay)'
  };
  return errors[code] || 'Giao dịch thất bại (Mã lỗi: ' + code + ')';
}

async function createVnpayUrl(req, res) {
  try {
    let { order_id, amount, bankCode } = req.body;
    const tmnCode = process.env.VNP_TMN_CODE;
    const secretKey = process.env.VNP_HASH_SECRET;
    let vnpUrl = process.env.VNP_URL;
    const returnUrl = process.env.VNP_RETURN_URL;

    const date = new Date();
    const createDate = moment(date).format('YYYYMMDDHHmmss');
    const expireDate = moment(date).add(15, 'minutes').format('YYYYMMDDHHmmss');

    let ipAddr = req.headers['x-forwarded-for'] || req.connection?.remoteAddress || '1.1.1.1';
    if (ipAddr.includes('::ffff:')) ipAddr = ipAddr.replace('::ffff:', '');

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
    vnp_Params['vnp_ExpireDate'] = expireDate;
    if (bankCode) vnp_Params['vnp_BankCode'] = bankCode;

    vnp_Params = sortObject(vnp_Params);
    const signData = queryString.stringify(vnp_Params, { encode: false });
    const hmac = crypto.createHmac("sha512", secretKey.trim());
    const signed = hmac.update(Buffer.from(signData, 'utf-8')).digest("hex");

    const finalUrl = vnpUrl + '?' + signData + '&vnp_SecureHash=' + signed;

    console.log(">>> [VNPAY CREATE] SignData:", signData);
    console.log(">>> [VNPAY CREATE] SecureHash:", signed);
    return res.json({ success: true, url: finalUrl });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
}

async function vnpayReturn(req, res) {
  try {
    let vnp_Params = req.query;
    const secureHash = vnp_Params['vnp_SecureHash'];
    const secretKey = process.env.VNP_HASH_SECRET;

    delete vnp_Params['vnp_SecureHash'];
    delete vnp_Params['vnp_SecureHashType'];

    vnp_Params = sortObject(vnp_Params);
    const signData = queryString.stringify(vnp_Params, { encode: false });
    const hmac = crypto.createHmac("sha512", secretKey.trim());
    const signed = hmac.update(Buffer.from(signData, 'utf-8')).digest("hex");

    if (secureHash === signed) {
      const order_id = vnp_Params['vnp_TxnRef'].split('_')[0];
      const responseCode = vnp_Params['vnp_ResponseCode'];

      if (responseCode === "00") {
        await createPayment({
          order_id: order_id,
          amount_paid: vnp_Params['vnp_Amount'] / 100,
          payment_method: 'VNPAY'
        });
        return res.send(`<html><body style="text-align:center;font-family:sans-serif;padding-top:50px;">
            <div style="color:#2ecc71;"><h2>Thanh toán thành công!</h2></div>
            <p>Đơn hàng ${order_id} đã được xác nhận.</p>
            <script>setTimeout(function(){ window.close(); }, 3000);</script>
        </body></html>`);
      } else {
        const errorMsg = getVnpayErrorMessage(responseCode);
        return res.send(`<html><body style="text-align:center;font-family:sans-serif;padding-top:50px;">
            <div style="color:#e74c3c;"><h2>Thanh toán thất bại</h2></div>
            <p>${errorMsg}</p>
            <button onclick="window.close()">Quay lại</button>
        </body></html>`);
      }
    } else {
      console.error(">>> [VNPAY RETURN ERROR] Chữ ký không khớp!");
      console.log("- SecureHash gửi về:", secureHash);
      console.log("- Signed tính toán:", signed);
      return res.status(400).send('Chữ ký không hợp lệ');
    }
  } catch (error) {
    return res.status(500).send(error.message);
  }
}

async function vnpayIpn(req, res) {
  try {
    let vnp_Params = req.query;
    const secureHash = vnp_Params['vnp_SecureHash'];
    const secretKey = process.env.VNP_HASH_SECRET;

    delete vnp_Params['vnp_SecureHash'];
    delete vnp_Params['vnp_SecureHashType'];
    vnp_Params = sortObject(vnp_Params);

    const signData = queryString.stringify(vnp_Params, { encode: false });
    const hmac = crypto.createHmac("sha512", secretKey.trim());
    const signed = hmac.update(Buffer.from(signData, 'utf-8')).digest("hex");

    if (secureHash === signed) {
      const order_id = vnp_Params['vnp_TxnRef'].split('_')[0];
      const responseCode = vnp_Params['vnp_ResponseCode'];

      if (responseCode === "00") {
        await createPayment({
          order_id: order_id,
          amount_paid: vnp_Params['vnp_Amount'] / 100,
          payment_method: 'VNPAY'
        });
      }
      res.status(200).json({ RspCode: '00', Message: 'Confirm Success' });
    } else {
      res.status(200).json({ RspCode: '97', Message: 'Fail checksum' });
    }
  } catch (error) {
    res.status(200).json({ RspCode: '99', Message: 'Error' });
  }
}

module.exports = { createVnpayUrl, vnpayReturn, vnpayIpn };
