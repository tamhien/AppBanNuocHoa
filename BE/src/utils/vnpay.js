/**
 * Sắp xếp và Encode tham số theo chuẩn VNPay 2.1.0
 * Dấu cách phải được chuyển thành dấu +
 */
function sortObject(obj) {
    let sorted = {};
    let str = [];
    let key;
    for (key in obj) {
        if (Object.prototype.hasOwnProperty.call(obj, key)) {
            str.push(encodeURIComponent(key));
        }
    }
    str.sort();
    for (key = 0; key < str.length; key++) {
        // Encode giá trị và thay thế %20 (dấu cách) thành +
        let val = obj[decodeURIComponent(str[key])];
        if (val !== undefined && val !== null) {
            sorted[str[key]] = encodeURIComponent(val).replace(/%20/g, "+");
        }
    }
    return sorted;
}

module.exports = { sortObject };
