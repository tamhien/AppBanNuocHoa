/**
 * Chỉ làm nhiệm vụ sắp xếp các key theo bảng chữ cái.
 * Việc mã hóa (Encode) sẽ được thực hiện ở tầng Controller bằng thư viện qs
 * để đảm bảo tính đồng nhất và tránh lỗi mã hóa 2 lần.
 */
function sortObject(obj) {
    let sorted = {};
    let keys = Object.keys(obj).sort();
    for (let key of keys) {
        sorted[key] = obj[key];
    }
    return sorted;
}

module.exports = { sortObject };
