# 📋 PROGRESS TODOLIST - PERFUME SHOP PROJECT

---

## 📦 Module 1: Xác thực & Tài khoản (Authentication & Account)
*Quản lý người dùng, phân quyền và hiển thị theo trạng thái đăng nhập*

### 🔑 Authentication (Tại màn hình Login/Register)
- [ x] `POST /api/register` : Đăng ký tài khoản (Role mặc định là 'user', gửi Password đã Hash).
- [ x] `POST /api/login` : Đăng nhập, trả về Role ('admin'/'user').
- [ ] `POST /api/forgot-password` : **Quên mật khẩu** (Xử lý tại màn hình Login).
- [ ] **UI Logic**: Hiển thị nút **Đăng nhập / Đăng ký** khi người dùng chưa đăng nhập.

### 👤 Account (Tại màn hình Trang chủ -> Tab Tài khoản)
- [ ] `GET /api/profile/:id` : Lấy thông tin cá nhân (Tên, SĐT, Email, Địa chỉ) từ DB.
- [ ] `PUT /api/profile/:id` : Cập nhật thông tin cá nhân cơ bản.
- [ ] `PUT /api/change-password/:id` : **Đổi mật khẩu** (Yêu cầu: MK hiện tại, MK mới, Nhập lại MK mới).
- [ ] `POST /api/logout` : **Đăng xuất** (Xóa session/token trên App).
- [ ] **UI Logic**: Chỉ hiển thị nút **Đăng xuất** và **Đổi mật khẩu** khi đã đăng nhập thành công.

---

## 📦 Module 2: Trang chủ & Sản phẩm (Home & Perfumes)
*Hiển thị sản phẩm và các bộ lọc cho User*

- [ x] `GET /api/perfumes` : Mặc định hiển thị tất cả sản phẩm.
- [ x] `GET /api/perfumes?gender=Men` : Lọc nước hoa nam.
- [ x] `GET /api/perfumes?gender=Women` : Lọc nước hoa nữ.
- [ x] `GET /api/perfumes?gender=Unisex` : Lọc nước hoa Unisex.
- [ x] `GET /api/perfumes?search=...` : Tìm kiếm theo Tên, Thương hiệu hoặc Giá.
- [ x] `GET /api/perfumes/:id` : Xem chi tiết sản phẩm.

---

## 📦 Module 3: Hệ thống Yêu thích (Favorites)
*Lưu trữ các sản phẩm quan tâm (Tab Yêu thích ở Footer)*

- [ x] `POST /api/favorites` : Thêm sản phẩm vào danh sách yêu thích.
- [ x] `GET /api/favorites/:userId` : Hiển thị các sản phẩm User đã "thả tim".
- [ x] `DELETE /api/favorites/:id` : Xóa khỏi danh sách yêu thích.

---

## 📦 Module 4: Giỏ hàng (Cart System)
*Xử lý trung gian trước khi thanh toán*

- [ ] `POST /api/cart/add` : Thêm sản phẩm vào giỏ hàng.
- [ ] `GET /api/cart/:userId` : Xem danh sách sản phẩm trong giỏ.
- [ ] `PUT /api/cart/update` : Cập nhật số lượng sản phẩm.
- [ ] `DELETE /api/cart/:cartId` : Xóa sản phẩm khỏi giỏ.

---

## 📦 Module 5: Đơn hàng & Lịch sử (Orders & History)
*Theo dõi trạng thái và lịch sử đặt hàng (Tab Lịch sử ở Footer)*

- [ ] `POST /api/checkout` : Thanh toán (Chốt đơn, trừ kho, xóa giỏ).
- [ ] `GET /api/orders/user/:userId` : Xem tất cả đơn hàng (Mặc định hiển thị 'All').
- [ ] `GET /api/orders/user/:userId?status=...` : Lọc đơn hàng theo trạng thái (Chờ duyệt, Đang giao, Thành công, Đã hủy).
- [ ] `GET /api/orders/detail/:orderId` : Xem chi tiết các món trong đơn hàng cũ.

---

## 📦 Module 6: Quản trị viên (Admin Management)
*Dành riêng cho người quản lý (Role: admin)*

- [ ] **Quản lý sản phẩm:**
    - [ x] `POST /api/perfumes` : Thêm nước hoa mới.
    - [ x] `PUT /api/perfumes/:id` : Chỉnh sửa thông tin/số lượng.
    - [ x] `DELETE /api/perfumes/:id` : Xóa sản phẩm khỏi cửa hàng.
- [ ] **Quản lý đơn hàng:**
    - [ ] `GET /api/admin/orders` : Xem toàn bộ đơn hàng của khách hàng.
    - [ ] `PUT /api/admin/orders/status` : Duyệt đơn và cập nhật trạng thái vận chuyển.
- [ ] **Quản lý khách hàng:**
    - [ x] `GET /api/admin/users` : Xem danh sách tất cả tài khoản khách hàng.
    - [ x] `DELETE /api/admin/users/:id` : Khóa hoặc xóa tài khoản khách hàng.
- [ ] **Thống kê:**
    - [ ] `GET /api/admin/revenue` : Thống kê doanh thu theo thời gian.

---
*Ghi chú: [x] = Đã có code xử lý, [ ] = Cần thực hiện tiếp.*
