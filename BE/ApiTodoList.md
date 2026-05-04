# 📋 PROGRESS TODOLIST - PERFUME SHOP API

---

## 📦 Module 1: Xác thực & Hồ sơ (Authentication & Profile)
*Dành cho cả Admin và Khách hàng*

- [ ] `POST /api/auth/register` : Đăng ký tài khoản khách hàng mới.
- [ ] `POST /api/auth/login` : Đăng nhập & Trả về Role (`admin` hoặc `user`).
- [ ] `GET /api/auth/profile/:id` : Lấy thông tin cá nhân của người dùng.
- [ ] `PUT /api/auth/profile/:id` : Cập nhật thông tin (Họ tên, SĐT, Địa chỉ).

---

## 📦 Module 2: Quản lý Sản phẩm (Perfumes)
*Admin quản lý kho, User xem và lọc theo nhu cầu*

- [ ] `GET /api/perfumes` : Lấy danh sách toàn bộ nước hoa (Trang chủ).
- [ ] `GET /api/perfumes/gender/:type` : Lọc nước hoa Nam (`Men`), Nữ (`Women`), hoặc `Unisex`.
- [ ] `GET /api/perfumes/:id` : Xem chi tiết thông tin 1 chai nước hoa.
- [ ] `POST /api/perfumes` : **(Admin)** Thêm nước hoa mới (Tên, Ảnh, Giá, Kho...).
- [ ] `PUT /api/perfumes/:id` : **(Admin)** Cập nhật thông tin hoặc số lượng tồn kho.
- [ ] `DELETE /api/perfumes/:id` : **(Admin)** Xóa sản phẩm khỏi cửa hàng.

---

## 📦 Module 3: Giỏ hàng (Cart System)
*Dành riêng cho Khách hàng mua sắm*

- [ ] `POST /api/cart/add` : Thêm nước hoa vào giỏ (Xử lý cộng dồn nếu sản phẩm đã có).
- [ ] `GET /api/cart/:userId` : Lấy danh sách sản phẩm đang nằm trong giỏ.
- [ ] `PUT /api/cart/update` : Cập nhật số lượng (+ hoặc -) trực tiếp trong giỏ hàng.
- [ ] `DELETE /api/cart/remove/:cartId` : Xóa một sản phẩm cụ thể khỏi giỏ hàng.

---

## 📦 Module 4: Đơn hàng & Thanh toán (Orders & Checkout)
*User đặt hàng chọn lọc, Admin quản lý quy trình giao hàng*

- [ ] `POST /api/orders/checkout` : **Thanh toán chọn lọc** (Chuyển các món được tick chọn từ Cart sang Orders).
- [ ] `GET /api/orders/user/:userId` : Khách hàng xem lịch sử các đơn hàng đã đặt.
- [ ] `GET /api/orders/admin/all` : **(Admin)** Xem toàn bộ đơn hàng của tất cả khách hàng.
- [ ] `PUT /api/orders/status` : **(Admin)** Duyệt đơn (Đổi trạng thái: Chờ duyệt -> Đang giao -> Thành công).

---

## 📦 Module 5: Thống kê & Quản trị (Admin Dashboard)
*Dành riêng cho Quản trị viên*

- [ ] `GET /api/admin/revenue` : Tính tổng doanh thu từ các đơn hàng thành công.
- [ ] `GET /api/admin/users` : Xem danh sách tất cả người dùng trong hệ thống.
- [ ] `DELETE /api/admin/users/:id` : Xóa hoặc vô hiệu hóa tài khoản khách hàng.

---
*Ghi chú: [ ] = Chưa làm, [x] = Đã hoàn thành.*