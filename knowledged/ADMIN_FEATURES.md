# 👨‍💼 CHỨC NĂNG ADMIN - 2BSHOP

> **Tài liệu này mô tả chi tiết tất cả chức năng quản trị (ADMIN) trong hệ thống 2BShop**

---

## 📋 MỤC LỤC

1. [Dashboard - Thống kê tổng quan](#1-dashboard---thống-kê-tổng-quan)
2. [User Management - Quản lý người dùng](#2-user-management---quản-lý-người-dùng)
3. [Watch Management - Quản lý sản phẩm](#3-watch-management---quản-lý-sản-phẩm)
4. [Brand Management - Quản lý thương hiệu](#4-brand-management---quản-lý-thương-hiệu)
5. [Order Management - Quản lý đơn hàng](#5-order-management---quản-lý-đơn-hàng)
6. [Payment Management - Quản lý thanh toán](#6-payment-management---quản-lý-thanh-toán)
7. [Bank Account Management - Quản lý tài khoản ngân hàng](#7-bank-account-management---quản-lý-tài-khoản-ngân-hàng)

---

## 1. DASHBOARD - THỐNG KÊ TỔNG QUAN

### 1.1. Admin Dashboard

**📌 Mục đích:** Hiển thị tổng quan về doanh thu, đơn hàng, sản phẩm

**🔗 Files liên quan:**
- **Controller:** `DashboardController.java`
  - Method: `dashboard()` - GET `/admin` hoặc `/admin/dashboard`
- **Service:** `DashboardService.java`
  - Method: `getRevenue(String period)` - Tính doanh thu theo period
  - Method: `getOrderCount(String period)` - Đếm đơn hàng
  - Method: `getProductCount()` - Đếm số sản phẩm
  - Method: `getUserCount()` - Đếm số user
  - Method: `getRevenueChartData(String period)` - Data cho biểu đồ
  - Method: `getTopSellingProducts()` - Top sản phẩm bán chạy
- **Template:** `templates/admin/dashboard.html`

**🎯 Model Attributes:**
```java
@RequestParam(defaultValue = "month") String period  // "day", "week", "month", "year"

model.addAttribute("revenue", BigDecimal)           // Tổng doanh thu
model.addAttribute("orderCount", int)               // Số đơn hàng
model.addAttribute("productCount", int)             // Số sản phẩm
model.addAttribute("userCount", int)                // Số user
model.addAttribute("chartLabels", List<String>)     // Labels cho chart (dates)
model.addAttribute("chartData", List<BigDecimal>)   // Data cho chart (revenue)
model.addAttribute("topProducts", List<Watch>)      // Top 5 sản phẩm bán chạy
```

**⚙️ Luồng xử lý:**
1. Admin truy cập `/admin` → GET `/admin/dashboard?period=month`
2. `dashboardService.getRevenue("month")` tính tổng revenue trong tháng
3. `dashboardService.getOrderCount("month")` đếm orders
4. `dashboardService.getRevenueChartData("month")` lấy data theo ngày
5. `dashboardService.getTopSellingProducts()` lấy top 5 products by soldCount
6. Render `dashboard.html` với statistics cards và charts

**📊 Dashboard Cards:**
```html
<!-- Card 1: Doanh thu -->
<div class="card revenue-card">
  <i class="fas fa-dollar-sign"></i>
  <h3 th:text="${#numbers.formatDecimal(revenue, 0, 'COMMA', 0, 'POINT')}">0</h3>
  <p>Doanh thu (tháng này)</p>
</div>

<!-- Card 2: Đơn hàng -->
<div class="card order-card">
  <i class="fas fa-shopping-cart"></i>
  <h3 th:text="${orderCount}">0</h3>
  <p>Đơn hàng (tháng này)</p>
</div>

<!-- Card 3: Sản phẩm -->
<div class="card product-card">
  <i class="fas fa-box"></i>
  <h3 th:text="${productCount}">0</h3>
  <p>Tổng sản phẩm</p>
</div>

<!-- Card 4: Người dùng -->
<div class="card user-card">
  <i class="fas fa-users"></i>
  <h3 th:text="${userCount}">0</h3>
  <p>Tổng người dùng</p>
</div>
```

**📈 Revenue Chart:**
```html
<canvas id="revenueChart"></canvas>
<script>
  // Data từ backend
  const labels = /*[[${chartLabels}]]*/ [];
  const data = /*[[${chartData}]]*/ [];
  
  // Chart.js render
  new Chart(ctx, {
    type: 'line',
    data: { labels, datasets: [{ data }] }
  });
</script>
```

---

## 2. USER MANAGEMENT - QUẢN LÝ NGƯỜI DÙNG

### 2.1. Danh sách users

**📌 Mục đích:** Hiển thị tất cả users với filter, search, pagination

**🔗 Files liên quan:**
- **Controller:** `UserAdminController.java`
  - Method: `listUsers()` - GET `/admin/users`
- **Service:** `UserService.java`
  - Method: `searchUsers(search, status, page, size)`
- **Template:** `templates/admin/users-new.html`

**🎯 Model Attributes:**
```java
@RequestParam(required = false) String search   // Tìm theo name/email
@RequestParam(required = false) String status   // "active", "banned", "unverified"
@RequestParam(defaultValue = "0") int page

model.addAttribute("users", Page<User>)          // Danh sách users (paging)
model.addAttribute("totalPages", int)
model.addAttribute("currentPage", int)
model.addAttribute("search", String)            // Giữ lại search keyword
model.addAttribute("selectedStatus", String)
```

**⚙️ Luồng xử lý:**
1. Admin truy cập `/admin/users?search=nguyen&status=active&page=0`
2. `userService.searchUsers()` query với filters
3. Return `Page<User>` với pagination
4. Render `users-new.html` với table

**🎨 UI Table:**
```html
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Avatar</th>
      <th>Full Name</th>
      <th>Email</th>
      <th>Phone</th>
      <th>Status</th>
      <th>Roles</th>
      <th>Actions</th>
    </tr>
  </thead>
  <tbody>
    <tr th:each="user : ${users.content}">
      <td th:text="${user.userId}"></td>
      <td><img th:src="${user.avatarUrl}" /></td>
      <td th:text="${user.fullName}"></td>
      <td th:text="${user.email}"></td>
      <td th:text="${user.phone}"></td>
      <td>
        <span th:if="${user.isBanned}" class="badge badge-danger">Banned</span>
        <span th:unless="${user.isBanned}" class="badge badge-success">Active</span>
      </td>
      <td th:text="${user.roles}"></td>
      <td>
        <a th:href="@{/admin/users/{id}/edit(id=${user.userId})}" class="btn btn-primary">Sửa</a>
        <a th:href="@{/admin/users/{id}/ban(id=${user.userId})}" class="btn btn-warning">Ban</a>
      </td>
    </tr>
  </tbody>
</table>
```

---

### 2.2. Thêm user mới

**📌 Mục đích:** Tạo user mới từ admin panel

**🔗 Files liên quan:**
- **Controller:** `UserAdminController.java`
  - Method: `newUserForm()` - GET `/admin/users/new`
  - Method: `saveUser()` - POST `/admin/users/save`
- **Service:** `UserService.java`
  - Method: `createUser(User user)`
- **Template:** `templates/admin/user-form.html`

**🎯 Model Attributes:**
```java
model.addAttribute("user", new User())     // Empty user for form binding
model.addAttribute("roles", List<Role>)    // Danh sách role để chọn
model.addAttribute("isNew", true)          // Flag để hiển thị/ẩn password field
```

**🎯 Form Fields:**
```java
@RequestParam String username
@RequestParam String email
@RequestParam String fullName
@RequestParam String phone
@RequestParam String address
@RequestParam String newPassword          // Mật khẩu (chỉ khi tạo mới)
@RequestParam List<String> roleNames      // Danh sách role được chọn (checkbox)
```

**⚙️ Luồng xử lý:**
1. Admin click "Thêm user" → GET `/admin/users/new`
2. Render form với User object rỗng
3. Admin điền form → POST `/admin/users/save`
4. `userAdminController.saveUser()`:
   - Validate username/email unique
   - Encode password với BCrypt
   - Assign roles
   - Save user
5. Redirect về `/admin/users` với flash message

---

### 2.3. Sửa thông tin user

**📌 Mục đích:** Cập nhật thông tin user

**🔗 Files liên quan:**
- **Controller:** `UserAdminController.java`
  - Method: `editUserForm()` - GET `/admin/users/{id}/edit`
  - Method: `saveUser()` - POST `/admin/users/save`
- **Service:** `UserService.java`
  - Method: `updateUser(User user)`

**🎯 Model Attributes:**
```java
@PathVariable Integer id

model.addAttribute("user", User)           // User hiện tại
model.addAttribute("roles", List<Role>)
model.addAttribute("isNew", false)         // Không hiển thị password field
model.addAttribute("currentRoles", List<String>)  // Roles hiện tại để pre-check
```

**⚙️ Luồng xử lý:**
1. Admin click "Sửa" → GET `/admin/users/5/edit`
2. `userService.findById(5)` lấy user
3. Render form với data hiện tại
4. Admin sửa → POST `/admin/users/save`
5. If `newPassword` not empty → Update password
6. Update roles nếu có thay đổi
7. Redirect về `/admin/users`

---

### 2.4. Ban/Unban user

**📌 Mục đích:** Khóa/mở khóa tài khoản user

**🔗 Files liên quan:**
- **Controller:** `UserAdminController.java`
  - Method: `banUser()` - POST `/admin/users/{id}/ban`
  - Method: `unbanUser()` - POST `/admin/users/{id}/unban`
- **Service:** `UserService.java`
  - Method: `banUser(Integer userId, String reason)`
  - Method: `unbanUser(Integer userId)`
- **Entity:** `BanHistory.java` (lưu lịch sử ban)

**🎯 Request Parameters:**
```java
@PathVariable Integer id
@RequestParam(required = false) String reason  // Lý do ban
```

**⚙️ Luồng xử lý:**
1. Admin click "Ban" → POST `/admin/users/5/ban`
2. `userService.banUser(5, reason)`:
   - Set `isBanned = true`
   - Create BanHistory record
   - Log ban action
3. Redirect về `/admin/users` với flash message

---

### 2.5. Xóa user

**📌 Mục đích:** Xóa user khỏi hệ thống (soft delete hoặc hard delete)

**🔗 Files liên quan:**
- **Controller:** `UserAdminController.java`
  - Method: `deleteUser()` - POST `/admin/users/{id}/delete`
- **Service:** `UserService.java`
  - Method: `deleteUser(Integer userId)`

**🎯 Request Parameters:**
```java
@PathVariable Integer id
```

**⚙️ Luồng xử lý:**
1. Admin click "Xóa" → POST `/admin/users/5/delete`
2. Kiểm tra user không có đơn hàng pending
3. `userService.deleteUser(5)` xóa user
4. Redirect về `/admin/users`

---

### 2.6. Xem lịch sử ban

**📌 Mục đích:** Xem danh sách users đã bị ban và lịch sử

**🔗 Files liên quan:**
- **Controller:** `UserAdminController.java`
  - Method: `banHistory()` - GET `/admin/users/ban-history`
- **Service:** `UserService.java`
  - Method: `getBanHistory()`
- **Template:** `templates/admin/ban-history.html`

**🎯 Model Attributes:**
```java
model.addAttribute("banHistory", List<BanHistory>)  // Danh sách ban logs
```

---

## 3. WATCH MANAGEMENT - QUẢN LÝ SẢN PHẨM

### 3.1. Danh sách sản phẩm

**📌 Mục đích:** Hiển thị tất cả sản phẩm với filter, search

**🔗 Files liên quan:**
- **Controller:** `WatchAdminController.java`
  - Method: `listWatches()` - GET `/admin/watches`
- **Service:** `WatchService.java`
  - Method: `searchWatchesAdmin(search, brandId, page, size)`
- **Template:** `templates/admin/watches.html`

**🎯 Model Attributes:**
```java
@RequestParam(required = false) String search
@RequestParam(required = false) Integer brandId
@RequestParam(defaultValue = "0") int page

model.addAttribute("watches", Page<Watch>)
model.addAttribute("brands", List<WatchBrand>)  // Cho filter dropdown
model.addAttribute("totalPages", int)
```

**⚙️ Luồng xử lý:**
1. Admin truy cập `/admin/watches?search=rolex&brandId=1`
2. `watchService.searchWatchesAdmin()` query với filters
3. Render table với pagination

**🎨 UI Table:**
```html
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Image</th>
      <th>Name</th>
      <th>Brand</th>
      <th>Price</th>
      <th>Stock</th>
      <th>Sold</th>
      <th>Status</th>
      <th>Actions</th>
    </tr>
  </thead>
  <tbody>
    <tr th:each="watch : ${watches.content}">
      <td th:text="${watch.watchId}"></td>
      <td><img th:src="${watch.imageUrl}" width="50" /></td>
      <td th:text="${watch.watchName}"></td>
      <td th:text="${watch.brand.brandName}"></td>
      <td th:text="${#numbers.formatDecimal(watch.price, 0, 'COMMA', 0, 'POINT')}"></td>
      <td th:text="${watch.stockQuantity}"></td>
      <td th:text="${watch.soldCount}"></td>
      <td>
        <span th:if="${watch.isActive}" class="badge badge-success">Active</span>
        <span th:unless="${watch.isActive}" class="badge badge-secondary">Inactive</span>
      </td>
      <td>
        <a th:href="@{/admin/watches/{id}/edit(id=${watch.watchId})}" class="btn btn-sm btn-primary">Sửa</a>
        <a th:href="@{/admin/watches/{id}/delete(id=${watch.watchId})}" class="btn btn-sm btn-danger">Xóa</a>
      </td>
    </tr>
  </tbody>
</table>
```

---

### 3.2. Thêm sản phẩm mới

**📌 Mục đích:** Tạo sản phẩm mới

**🔗 Files liên quan:**
- **Controller:** `WatchAdminController.java`
  - Method: `newWatchForm()` - GET `/admin/watches/new`
  - Method: `saveWatch()` - POST `/admin/watches/save`
- **Service:** `WatchService.java`
  - Method: `createWatch(Watch watch)`
- **Service:** `FileUploadService.java`
  - Method: `uploadWatchImage(MultipartFile file)`
- **Template:** `templates/admin/watch-form-new.html`

**🎯 Model Attributes:**
```java
model.addAttribute("watch", new Watch())
model.addAttribute("brands", List<WatchBrand>)  // Dropdown brands
model.addAttribute("isNew", true)
```

**🎯 Form Fields:**
```java
@RequestParam String watchName
@RequestParam Integer brandId
@RequestParam BigDecimal price
@RequestParam int stockQuantity
@RequestParam String description
@RequestParam(required = false) Integer discountPercent
@RequestParam("imageFile") MultipartFile imageFile  // Upload hình
@RequestParam Boolean isActive
```

**⚙️ Luồng xử lý:**
1. Admin click "Thêm sản phẩm" → GET `/admin/watches/new`
2. Render form rỗng
3. Admin điền form + upload image → POST `/admin/watches/save`
4. `fileUploadService.uploadWatchImage()` save image
5. Set `imageUrl` từ file uploaded
6. `watchService.createWatch()` save DB
7. Redirect về `/admin/watches` với flash message

---

### 3.3. Sửa sản phẩm

**📌 Mục đích:** Cập nhật thông tin sản phẩm

**🔗 Files liên quan:**
- **Controller:** `WatchAdminController.java`
  - Method: `editWatchForm()` - GET `/admin/watches/{id}/edit`
  - Method: `saveWatch()` - POST `/admin/watches/save`
- **Service:** `WatchService.java`
  - Method: `updateWatch(Watch watch)`

**🎯 Model Attributes:**
```java
@PathVariable Integer id

model.addAttribute("watch", Watch)         // Watch hiện tại
model.addAttribute("brands", List<WatchBrand>)
model.addAttribute("isNew", false)
```

**⚙️ Luồng xử lý:**
1. Admin click "Sửa" → GET `/admin/watches/5/edit`
2. `watchService.findById(5)` lấy watch
3. Render form với data hiện tại
4. Admin sửa → POST `/admin/watches/save`
5. If có upload image mới → Replace old image
6. Update DB
7. Redirect về `/admin/watches`

---

### 3.4. Xóa sản phẩm

**📌 Mục đích:** Xóa sản phẩm (soft delete: set isActive = false)

**🔗 Files liên quan:**
- **Controller:** `WatchAdminController.java`
  - Method: `deleteWatch()` - POST `/admin/watches/{id}/delete`
- **Service:** `WatchService.java`
  - Method: `deleteWatch(Integer watchId)`

**⚙️ Luồng xử lý:**
1. Admin click "Xóa" → POST `/admin/watches/5/delete`
2. `watchService.deleteWatch(5)` set `isActive = false`
3. Redirect về `/admin/watches`

---

### 3.5. Cập nhật stock

**📌 Mục đích:** Thay đổi số lượng tồn kho

**🔗 Files liên quan:**
- **Controller:** `WatchAdminController.java`
  - Method: `updateStock()` - POST `/admin/watches/{id}/stock`
- **Service:** `WatchService.java`
  - Method: `updateStock(Integer watchId, int quantity)`

**🎯 Request Parameters:**
```java
@PathVariable Integer id
@RequestParam int quantity  // Số lượng mới
```

**⚙️ Luồng xử lý:**
1. Admin nhập số lượng → POST `/admin/watches/5/stock`
2. `watchService.updateStock(5, 100)` cập nhật `stockQuantity`
3. Return JSON response

---

## 4. BRAND MANAGEMENT - QUẢN LÝ THƯƠNG HIỆU

### 4.1. Danh sách brands

**📌 Mục đích:** Hiển thị tất cả brands

**🔗 Files liên quan:**
- **Controller:** `BrandAdminController.java`
  - Method: `listBrands()` - GET `/admin/brands`
- **Service:** `WatchService.java`
  - Method: `getAllBrands()`
- **Template:** `templates/admin/brands.html`

**🎯 Model Attributes:**
```java
model.addAttribute("brands", List<WatchBrand>)
```

---

### 4.2. Thêm/Sửa/Xóa brand

**📌 Mục đích:** CRUD operations cho brands

**🔗 Files liên quan:**
- **Controller:** `BrandAdminController.java`
  - Method: `saveBrand()` - POST `/admin/brands/save`
  - Method: `deleteBrand()` - POST `/admin/brands/{id}/delete`
- **Service:** `WatchService.java`
  - Method: `saveBrand(WatchBrand brand)`
  - Method: `deleteBrand(Integer brandId)`

**🎯 Form Fields:**
```java
@RequestParam String brandName
@RequestParam String description
@RequestParam Boolean isActive
```

---

## 5. ORDER MANAGEMENT - QUẢN LÝ ĐƠN HÀNG

### 5.1. Danh sách đơn hàng

**📌 Mục đích:** Hiển thị tất cả orders với filter theo status

**🔗 Files liên quan:**
- **Controller:** `OrderAdminController.java`
  - Method: `listOrders()` - GET `/admin/orders`
- **Service:** `OrderService.java`
  - Method: `searchOrders(search, status, page, size)`
- **Template:** `templates/admin/orders-new.html`

**🎯 Model Attributes:**
```java
@RequestParam(required = false) String search   // Tìm theo mã đơn/tên user
@RequestParam(required = false) String status   // PENDING, CONFIRMED, SHIPPING, DELIVERED, etc.
@RequestParam(defaultValue = "0") int page

model.addAttribute("orders", Page<Order>)
model.addAttribute("totalPages", int)
model.addAttribute("selectedStatus", String)
```

**⚙️ Luồng xử lý:**
1. Admin truy cập `/admin/orders?status=PENDING&page=0`
2. `orderService.searchOrders()` filter theo status
3. Render table với pagination

**🎨 UI Table:**
```html
<table>
  <thead>
    <tr>
      <th>Mã ĐH</th>
      <th>Khách hàng</th>
      <th>Người nhận</th>
      <th>SĐT</th>
      <th>Ngày đặt</th>
      <th>Tổng tiền</th>
      <th>Thanh toán</th>
      <th>Trạng thái</th>
      <th>Hành động</th>
    </tr>
  </thead>
  <tbody>
    <tr th:each="order : ${orders.content}">
      <td th:text="${'ORD' + #strings.substring('00000' + order.orderId, -6)}"></td>
      <td th:text="${order.user.fullName}"></td>
      <td th:text="${order.receiverName}"></td>
      <td th:text="${order.shippingPhone}"></td>
      <td th:text="${#temporals.format(order.orderDate, 'dd/MM/yyyy HH:mm')}"></td>
      <td th:text="${#numbers.formatDecimal(order.totalAmount, 0, 'COMMA', 0, 'POINT')} + '₫'"></td>
      <td th:text="${order.paymentMethod.methodName}"></td>
      <td>
        <span th:class="'badge badge-' + ${order.orderStatus}" th:text="${order.orderStatus}"></span>
      </td>
      <td>
        <a th:href="@{/admin/orders/{id}(id=${order.orderId})}" class="btn btn-sm btn-info">Chi tiết</a>
      </td>
    </tr>
  </tbody>
</table>
```

---

### 5.2. Chi tiết đơn hàng

**📌 Mục đích:** Xem thông tin chi tiết 1 đơn hàng

**🔗 Files liên quan:**
- **Controller:** `OrderAdminController.java`
  - Method: `orderDetail()` - GET `/admin/orders/{id}`
- **Service:** `OrderService.java`
  - Method: `findById(Integer orderId)`
- **Template:** `templates/admin/order-detail-new.html`

**🎯 Model Attributes:**
```java
@PathVariable Integer id

model.addAttribute("order", Order)
model.addAttribute("orderDetails", List<OrderDetail>)
model.addAttribute("user", User)                  // Thông tin khách hàng
model.addAttribute("canUpdateStatus", boolean)    // Có thể đổi status hay không
```

**⚙️ Luồng xử lý:**
1. Admin click "Chi tiết" → GET `/admin/orders/5`
2. `orderService.findById(5)` lấy order + details
3. Render order detail với thông tin đầy đủ

---

### 5.3. Cập nhật trạng thái đơn hàng

**📌 Mục đích:** Thay đổi status của order (PENDING → CONFIRMED → SHIPPING → DELIVERED)

**🔗 Files liên quan:**
- **Controller:** `OrderAdminController.java`
  - Method: `updateOrderStatus()` - POST `/admin/orders/{id}/status`
- **Service:** `OrderService.java`
  - Method: `updateOrderStatus(Integer orderId, String newStatus)`
- **Service:** `MailService.java`
  - Method: `sendShippingEmail()`, `sendDeliveredEmail()`, etc.

**🎯 Request Parameters:**
```java
@PathVariable Integer id
@RequestParam String status  // New status
```

**⚙️ Luồng xử lý:**
1. Admin chọn status → POST `/admin/orders/5/status`
2. `orderService.updateOrderStatus(5, "SHIPPING")`:
   - Validate status transition (PENDING → CONFIRMED → SHIPPING → DELIVERED → COMPLETED)
   - Update `orderStatus`
   - Send email notification → `mailService.sendShippingEmail()`
3. Redirect về `/admin/orders/5`

**📧 Email Notifications:**
- CONFIRMED → Email: "Đơn hàng đã được xác nhận"
- SHIPPING → Email: "Đơn hàng đang giao"
- DELIVERED → Email: "Đơn hàng đã giao thành công"
- COMPLETED → Email: "Cảm ơn bạn đã mua hàng"
- CANCELLED → Email: "Đơn hàng đã bị hủy"

---

### 5.4. Hủy đơn hàng

**📌 Mục đích:** Admin hủy đơn hàng

**🔗 Files liên quan:**
- **Controller:** `OrderAdminController.java`
  - Method: `cancelOrder()` - POST `/admin/orders/{id}/cancel`
- **Service:** `OrderService.java`
  - Method: `cancelOrder(Integer orderId, String reason)`

**🎯 Request Parameters:**
```java
@PathVariable Integer id
@RequestParam String reason  // Lý do hủy
```

**⚙️ Luồng xử lý:**
1. Admin click "Hủy" → POST `/admin/orders/5/cancel`
2. `orderService.cancelOrder(5, reason)`:
   - Set `orderStatus = CANCELLED`
   - Hoàn lại stock
   - Giảm soldCount
   - Send email
3. Redirect về `/admin/orders`

---

### 5.5. In hóa đơn

**📌 Mục đích:** Xuất hóa đơn PDF/Word cho đơn hàng

**🔗 Files liên quan:**
- **Controller:** `InvoiceController.java`
  - Method: `downloadWordInvoice()` - GET `/invoice/{orderId}/word`
  - Method: `downloadPdfInvoice()` - GET `/invoice/{orderId}/pdf`
- **Service:** `InvoiceService.java`
  - Method: `generateWordInvoice(Integer orderId)`
  - Method: `generatePdfInvoice(Integer orderId)`

---

## 6. PAYMENT MANAGEMENT - QUẢN LÝ THANH TOÁN

### 6.1. Danh sách phương thức thanh toán

**📌 Mục đích:** Quản lý payment methods (COD, VNPay, Banking)

**🔗 Files liên quan:**
- **Controller:** `PaymentAdminController.java`
  - Method: `listPaymentMethods()` - GET `/admin/payment-methods`
- **Service:** `PaymentMethodService.java`
  - Method: `getAllPaymentMethods()`
- **Template:** `templates/admin/payment-methods.html`

**🎯 Model Attributes:**
```java
model.addAttribute("paymentMethods", List<PaymentMethod>)
```

**🎨 UI:**
```html
<table>
  <thead>
    <tr>
      <th>ID</th>
      <th>Method Name</th>
      <th>Description</th>
      <th>Status</th>
      <th>Actions</th>
    </tr>
  </thead>
  <tbody>
    <tr th:each="pm : ${paymentMethods}">
      <td th:text="${pm.paymentMethodId}"></td>
      <td th:text="${pm.methodName}"></td>
      <td th:text="${pm.description}"></td>
      <td>
        <span th:if="${pm.isActive}" class="badge badge-success">Active</span>
        <span th:unless="${pm.isActive}" class="badge badge-secondary">Inactive</span>
      </td>
      <td>
        <a th:href="@{/admin/payment-methods/{id}/edit(id=${pm.paymentMethodId})}" class="btn btn-sm btn-primary">Sửa</a>
        <a th:href="@{/admin/payment-methods/{id}/toggle(id=${pm.paymentMethodId})}" class="btn btn-sm btn-warning">
          <span th:text="${pm.isActive ? 'Disable' : 'Enable'}"></span>
        </a>
      </td>
    </tr>
  </tbody>
</table>
```

---

### 6.2. Thêm/Sửa payment method

**📌 Mục đích:** CRUD operations cho payment methods

**🔗 Files liên quan:**
- **Controller:** `PaymentAdminController.java`
  - Method: `savePaymentMethod()` - POST `/admin/payment-methods/save`
- **Service:** `PaymentMethodService.java`
  - Method: `savePaymentMethod(PaymentMethod pm)`

**🎯 Form Fields:**
```java
@RequestParam String methodName     // COD, VNPAY, BANK_TRANSFER
@RequestParam String description    // Mô tả
@RequestParam Boolean isActive      // Bật/tắt
```

---

### 6.3. Xem lịch sử giao dịch

**📌 Mục đích:** Xem tất cả payment transactions

**🔗 Files liên quan:**
- **Controller:** `PaymentAdminController.java`
  - Method: `transactionHistory()` - GET `/admin/payment-transactions`
- **Service:** `PaymentService.java`
  - Method: `getAllTransactions(page, size)`
- **Template:** `templates/admin/payment-transactions.html`

**🎯 Model Attributes:**
```java
model.addAttribute("transactions", Page<PaymentTransaction>)
```

**🎨 UI:**
```html
<table>
  <thead>
    <tr>
      <th>Transaction ID</th>
      <th>Order ID</th>
      <th>Payment Method</th>
      <th>Amount</th>
      <th>Status</th>
      <th>Date</th>
    </tr>
  </thead>
  <tbody>
    <tr th:each="txn : ${transactions.content}">
      <td th:text="${txn.transactionId}"></td>
      <td th:text="${txn.order.orderId}"></td>
      <td th:text="${txn.paymentMethod.methodName}"></td>
      <td th:text="${#numbers.formatDecimal(txn.amount, 0, 'COMMA', 0, 'POINT')} + '₫'"></td>
      <td>
        <span th:class="'badge badge-' + ${txn.status}" th:text="${txn.status}"></span>
      </td>
      <td th:text="${#temporals.format(txn.transactionDate, 'dd/MM/yyyy HH:mm')}"></td>
    </tr>
  </tbody>
</table>
```

---

## 7. BANK ACCOUNT MANAGEMENT - QUẢN LÝ TÀI KHOẢN NGÂN HÀNG

### 7.1. Danh sách tài khoản ngân hàng

**📌 Mục đích:** Quản lý tài khoản ngân hàng để nhận chuyển khoản

**🔗 Files liên quan:**
- **Controller:** `BankAdminController.java`
  - Method: `listBankAccounts()` - GET `/admin/bank-accounts`
- **Service:** `BankAccountService.java`
  - Method: `getAllBankAccounts()`
- **Template:** `templates/admin/bank-accounts.html`

**🎯 Model Attributes:**
```java
model.addAttribute("bankAccounts", List<BankAccount>)
```

**🎨 UI (Card Grid View):**
```html
<div class="row">
  <div class="col-md-6 col-lg-4" th:each="bank : ${bankAccounts}">
    <div class="bank-card">
      <div class="bank-header">
        <h5 th:text="${bank.bankName}">Vietcombank</h5>
        <span th:if="${bank.isActive}" class="badge badge-success">Active</span>
        <span th:unless="${bank.isActive}" class="badge badge-secondary">Inactive</span>
      </div>
      <div class="bank-body">
        <img th:src="${bank.qrCodeUrl}" class="bank-qr" alt="QR Code" />
        <p><strong>Ngân hàng:</strong> <span th:text="${bank.bankCode}">VCB</span></p>
        <p><strong>STK:</strong> <span th:text="${bank.accountNumber}">1234567890</span></p>
        <p><strong>Chủ TK:</strong> <span th:text="${bank.accountHolder}">NGUYEN VAN A</span></p>
        <p><strong>Thứ tự:</strong> <span th:text="${bank.displayOrder}">1</span></p>
      </div>
      <div class="bank-actions">
        <button onclick="openEditModal(${bank.bankAccountId})" class="btn btn-sm btn-primary">Sửa</button>
        <button onclick="deleteBank(${bank.bankAccountId})" class="btn btn-sm btn-danger">Xóa</button>
      </div>
    </div>
  </div>
</div>
```

---

### 7.2. Thêm ngân hàng mới (Modal AJAX)

**📌 Mục đích:** Tạo tài khoản ngân hàng mới

**🔗 Files liên quan:**
- **Controller:** `BankAdminController.java`
  - Method: `saveBankAccount()` - POST `/admin/bank-accounts/api/save` (REST API)
- **Service:** `BankAccountService.java`
  - Method: `saveBankAccount(BankAccount bankAccount)`
  - Method: `generateAndSaveQrCode(BankAccount bankAccount)`
- **Template:** `templates/admin/bank-accounts.html` (Modal form)

**🎯 Request (JSON):**
```json
{
  "bankName": "Vietcombank",
  "bankCode": "VCB",
  "accountNumber": "1234567890",
  "accountHolder": "NGUYEN VAN A",
  "displayOrder": 1,
  "isActive": true
}
```

**🎯 Response (JSON):**
```json
{
  "success": true,
  "message": "Thêm ngân hàng thành công",
  "bankAccountId": 5
}
```

**⚙️ Luồng xử lý:**
1. Admin click "Thêm ngân hàng" → Mở modal form
2. Điền form → Submit AJAX POST `/admin/bank-accounts/api/save`
3. `bankAccountService.saveBankAccount()` lưu DB
4. `bankAccountService.generateAndSaveQrCode()`:
   - Call VietQR API: `https://img.vietqr.io/image/{BANK_CODE}-{ACCOUNT_NO}-compact2.png`
   - Download QR image
   - Save to `C:/uploads/bshop/banking/bank_{id}.png`
   - Update `qrImageUrl` trong DB
5. Return JSON response
6. JavaScript reload page hoặc append card mới

**🔧 QR Code Generation:**
```java
// BankAccountService.java
public void generateAndSaveQrCode(BankAccount bankAccount) {
    String apiUrl = String.format(
        "https://img.vietqr.io/image/%s-%s-compact2.png?addInfo=Thanh toan don hang&accountName=%s",
        bankAccount.getBankCode(),
        bankAccount.getAccountNumber(),
        bankAccount.getAccountHolder().replace(" ", "%20")
    );
    
    RestTemplate restTemplate = new RestTemplate();
    byte[] imageBytes = restTemplate.getForObject(apiUrl, byte[].class);
    
    String fileName = "bank_" + bankAccount.getBankAccountId() + ".png";
    Path uploadPath = Paths.get("C:/uploads/bshop/banking");
    Files.createDirectories(uploadPath);
    Files.write(uploadPath.resolve(fileName), imageBytes);
    
    bankAccount.setQrImageUrl("/uploads/banking/" + fileName);
    bankAccountRepository.save(bankAccount);
}
```

---

### 7.3. Sửa thông tin ngân hàng (Modal)

**📌 Mục đích:** Cập nhật thông tin bank account

**🔗 Files liên quan:**
- **Controller:** `BankAdminController.java`
  - Method: `getBankAccountById()` - GET `/admin/bank-accounts/api/{id}` (REST API)
  - Method: `saveBankAccount()` - POST `/admin/bank-accounts/api/save`

**⚙️ Luồng xử lý:**
1. Admin click "Sửa" → AJAX GET `/admin/bank-accounts/api/5`
2. Return JSON → Populate modal form
3. Sửa → POST `/admin/bank-accounts/api/save`
4. Update DB và regenerate QR nếu cần
5. Return JSON response

---

### 7.4. Xóa ngân hàng (AJAX)

**📌 Mục đích:** Xóa bank account

**🔗 Files liên quan:**
- **Controller:** `BankAdminController.java`
  - Method: `deleteBankAccount()` - DELETE `/admin/bank-accounts/api/delete/{id}`
- **Service:** `BankAccountService.java`
  - Method: `deleteBankAccount(Integer id)`

**🎯 Response (JSON):**
```json
{
  "success": true,
  "message": "Đã xóa ngân hàng"
}
```

**⚙️ Luồng xử lý:**
1. Admin click "Xóa" → Confirm dialog
2. AJAX DELETE `/admin/bank-accounts/api/delete/5`
3. `bankAccountService.deleteBankAccount(5)` xóa DB
4. Xóa file QR code
5. Return JSON
6. JavaScript remove card khỏi UI

---

## 📊 TỔNG KẾT CÁC CONTROLLER ADMIN SỬ DỤNG

| Controller | Base Path | Main Functions |
|------------|-----------|----------------|
| `DashboardController` | `/admin` | Dashboard, Statistics, Charts |
| `UserAdminController` | `/admin/users` | User CRUD, Ban/Unban |
| `WatchAdminController` | `/admin/watches` | Watch CRUD, Stock management |
| `BrandAdminController` | `/admin/brands` | Brand CRUD |
| `OrderAdminController` | `/admin/orders` | Order management, Status updates |
| `PaymentAdminController` | `/admin/payment-methods` | Payment method CRUD, Transactions |
| `BankAdminController` | `/admin/bank-accounts` | Bank account CRUD, QR generation |

---

## 🔐 SECURITY & AUTHORIZATION

**Spring Security Configuration:**
- Admin paths: `/admin/**` → Require `ROLE_ADMIN`
- Admin login → Redirect to `/admin/dashboard`
- Non-admin users → Access denied (403)

**AdminController @RequestMapping:**
```java
@Controller
@RequestMapping("/admin/...")
public class AdminController {
    // Tất cả methods require ROLE_ADMIN
}
```

---

## 🎨 UI TEMPLATES & MODEL ATTRIBUTES

### Admin Layout:
- Base template: `templates/admin/layout/admin-layout.html`
- Sidebar menu với highlight active page
- CSRF token trong meta tag cho AJAX

### Common Model Attributes:
```java
model.addAttribute("pageTitle", String)        // Title trang admin
model.addAttribute("activeMenu", String)       // Để highlight menu item
model.addAttribute("success", String)          // Flash success message
model.addAttribute("error", String)            // Flash error message
```

---

## 📧 ADMIN EMAIL NOTIFICATIONS

Admin nhận email trong các trường hợp:
1. **Đơn hàng mới** → Thông báo có đơn mới cần xử lý
2. **User abuse report** → Báo cáo vi phạm
3. **System errors** → Thông báo lỗi hệ thống
4. **Low stock alert** → Cảnh báo hết hàng

---

## 🔄 DATA FLOW SUMMARY

```
1. ADMIN ACTION (Click/Submit form)
   ↓
2. ADMIN CONTROLLER (Validate permissions)
   ↓
3. SERVICE LAYER (Business logic)
   ↓
4. REPOSITORY (Database operations)
   ↓
5. ENTITY (Update domain objects)
   ↓
6. SERVICE (Return result)
   ↓
7. CONTROLLER (Flash message, redirect)
   ↓
8. VIEW TEMPLATE (Render admin page)
   ↓
9. RESPONSE (Admin panel HTML)
```

---

## 🛠️ AJAX vs TRADITIONAL FORM

### Traditional Form (Page reload):
- User/Watch CRUD → POST with redirect
- Order status update → POST with redirect

### AJAX (No reload):
- Bank account CRUD → REST API với JSON response
- Quick actions (ban/unban, activate/deactivate)
- Stock quantity update

### AJAX Pattern:
```javascript
// Frontend
fetch('/admin/bank-accounts/api/save', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        [csrfHeader]: csrfToken  // CSRF protection
    },
    body: JSON.stringify(bankData)
})
.then(res => res.json())
.then(data => {
    if (data.success) {
        // Update UI
    }
});

// Backend
@PostMapping("/api/save")
@ResponseBody
public Map<String, Object> saveBankAccount(@RequestBody BankAccount bankAccount) {
    Map<String, Object> response = new HashMap<>();
    try {
        bankAccountService.saveBankAccount(bankAccount);
        response.put("success", true);
        response.put("message", "Lưu thành công");
    } catch (Exception e) {
        response.put("success", false);
        response.put("message", e.getMessage());
    }
    return response;
}
```

---

**📝 Ghi chú:** Tài liệu này mô tả tất cả chức năng ADMIN. Để xem chức năng USER, tham khảo `USER_FEATURES.md`.
