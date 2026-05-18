const db = require('../config/db');
const { sortObject } = require('../utils/vnpay');
const { createPayment } = require('../services/payment.service');
const moment = require('moment');
const crypto = require('crypto');
const queryString = require('qs');

async function createVnpayUrl(req, res) {
  try {
    let { order_id, amount, bankCode } = req.body;
    const tmnCode = process.env.VNP_TMN_CODE;
    const secretKey = process.env.VNP_HASH_SECRET;
    let vnpUrl = process.env.VNP_URL;
    const returnUrl = process.env.VNP_RETURN_URL;

    const date = new Date();
    const createDate = moment(date).format('YYYYMMDDHHmmss');

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

    // Quy đổi $: 1$ = 25.000 VNĐ
    vnp_Params['vnp_Amount'] = Math.floor(parseFloat(amount) * 25000) * 100;

    vnp_Params['vnp_ReturnUrl'] = returnUrl.trim();
    vnp_Params['vnp_IpAddr'] = ipAddr;
    vnp_Params['vnp_CreateDate'] = createDate;
    if (bankCode) vnp_Params['vnp_BankCode'] = bankCode;

    // Sắp xếp tham số
    vnp_Params = sortObject(vnp_Params);

    // BƯỚC QUAN TRỌNG: Tạo chuỗi băm SignData (KHÔNG Encode giá trị)
    const signData = queryString.stringify(vnp_Params, { encode: false });

    // Tạo mã băm SecureHash
    const hmac = crypto.createHmac("sha512", secretKey.trim());
    const signed = hmac.update(Buffer.from(signData, 'utf-8')).digest("hex");

    vnp_Params['vnp_SecureHash'] = signed;

    // Tạo URL cuối cùng (CÓ Encode giá trị)
    const finalUrl = vnpUrl + '?' + queryString.stringify(vnp_Params, { encode: true });

    console.log(">>> [VNPAY] SignData (Chuẩn):", signData);
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

    // Kiểm tra chữ ký lúc quay về cũng KHÔNG được encode
    const signData = queryString.stringify(vnp_Params, { encode: false });
    const hmac = crypto.createHmac("sha512", secretKey.trim());
    const signed = hmac.update(Buffer.from(signData, 'utf-8')).digest("hex");

    if (secureHash === signed) {
      const order_id = vnp_Params['vnp_TxnRef'].split('_')[0];
      if (vnp_Params['vnp_ResponseCode'] === "00") {
        await createPayment({
          order_id: order_id,
          amount_paid: vnp_Params['vnp_Amount'] / 100,
          payment_method: 'VNPAY'
        });
        return res.send(`<html><body style="text-align:center;font-family:sans-serif;padding-top:50px;">
            <div style="color:#2ecc71;"><h2>Thanh toán thành công!</h2></div>
            <script>setTimeout(function(){ window.close(); }, 3000);</script>
        </body></html>`);
      }
      return res.send(`<h1>Thanh toán thất bại</h1><p>Mã lỗi: ${vnp_Params['vnp_ResponseCode']}</p>`);
    }
    return res.status(400).send('Chữ ký không hợp lệ');
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
      if (vnp_Params['vnp_ResponseCode'] === "00") {
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
