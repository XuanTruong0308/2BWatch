# 🧪 TESTING GUIDE - 2BSHOP

**Ngày cập nhật:** 14/01/2026  
**Mục đích:** Hướng dẫn test đầy đủ các chức năng đã hoàn thành

---

## 🎯 TESTING OVERVIEW

### ✅ User-Facing Features (65% - CẦN TEST)
- Homepage
- Product Listing & Detail
- Cart & Checkout
- Order Management
- User Profile
- Email System

### ❌ Admin Features (35% - CHƯA CÓ)
- Dashboard
- Watch Management
- Order Management
- User Management

---

## 🚀 PREPARATION

### 1. Khởi động Database
```bash
# Đảm bảo SQL Server đang chạy
# Database: BoizShop
# User: sa
```

### 2. Khởi động Spring Boot
```bash
cd d:\BoizShop\2BShop
mvn clean install
mvn spring-boot:run
```

**Kiểm tra logs:**
```
Started Application in X seconds
Tomcat started on port 8080
```

### 3. Truy cập Application
```
http://localhost:8080
```

---

## 📋 TEST CASES

## 1️⃣ HOMEPAGE TESTS

### Test 1.1: Load Homepage
**Steps:**
1. Truy cập `http://localhost:8080/`
2. Kiểm tra page load thành công

**Expected:**
- ✅ Header hiển thị: Logo, Navigation menu, Cart icon
- ✅ Hero section với CTA button
- ✅ 3 sections: Best Sellers, Newest, Biggest Discount
- ✅ Footer hiển thị đầy đủ

**SQL Verify:**
```sql
-- Check có sản phẩm không
SELECT COUNT(*) FROM Watches WHERE is_active = 1;

-- Check Top 3 Best Sellers
SELECT TOP 3 * FROM Watches 
WHERE is_active = 1 
ORDER BY sold_count DESC;

-- Check Top 3 Newest
SELECT TOP 3 * FROM Watches 
WHERE is_active = 1 
ORDER BY created_date DESC;

-- Check Top 3 Discount
SELECT TOP 3 * FROM Watches 
WHERE is_active = 1 
ORDER BY discount_percent DESC;
```

---

### Test 1.2: Navigation Menu
**Steps:**
1. Click vào "Sản phẩm" trong menu
2. Click vào "Giới thiệu"
3. Click vào "Liên hệ"

**Expected:**
- ✅ Redirect đúng URL
- ✅ Smooth transition

---

### Test 1.3: Product Cards in Homepage
**Steps:**
1. Hover vào product card
2. Click vào product image/name

**Expected:**
- ✅ Hover effect: zoom image, shadow
- ✅ Redirect to product detail page

---

## 2️⃣ PRODUCT LISTING TESTS

### Test 2.1: View All Products
**Steps:**
1. Truy cập `http://localhost:8080/watches`

**Expected:**
- ✅ Hiển thị tất cả sản phẩm active
- ✅ Grid layout: 3 columns (desktop)
- ✅ Pagination hiển thị nếu > 12 sản phẩm
- ✅ Product card hiển thị: image, name, brand, price, discount badge

**SQL Verify:**
```sql
SELECT COUNT(*) FROM Watches WHERE is_active = 1;
-- Ví dụ: 20 products → 2 pages (12 per page)
```

---

### Test 2.2: Search Products
**Steps:**
1. Nhập "Rolex" vào search box
2. Đợi 800ms (auto-submit)
3. Kiểm tra kết quả

**Expected:**
- ✅ Chỉ hiển thị sản phẩm có tên hoặc brand chứa "Rolex"
- ✅ URL: `?keyword=Rolex`

**SQL Verify:**
```sql
SELECT * FROM Watches 
WHERE is_active = 1 
AND (watch_name LIKE '%Rolex%' OR brand_id IN (
    SELECT brand_id FROM WatchBrands WHERE brand_name LIKE '%Rolex%'
));
```

---

### Test 2.3: Filter by Brand
**Steps:**
1. Click vào filter "Brand: Omega"

**Expected:**
- ✅ Chỉ hiển thị sản phẩm Omega
- ✅ URL: `?brand=Omega`

---

### Test 2.4: Filter by Category
**Steps:**
1. Click vào filter "Category: Sport Watch"

**Expected:**
- ✅ Chỉ hiển thị Sport Watch
- ✅ URL: `?category=Sport Watch`

---

### Test 2.5: Add to Cart from Product List
**Steps:**
1. Click "Thêm vào giỏ" trên product card
2. Kiểm tra notification
3. Kiểm tra cart badge

**Expected:**
- ✅ Notification: "Đã thêm vào giỏ hàng!"
- ✅ Cart badge tăng +1
- ✅ Không reload page (AJAX)

**SQL Verify:**
```sql
-- Check cart item được tạo
SELECT * FROM CartItems 
WHERE cart_id = (SELECT cart_id FROM Carts WHERE user_id = [USER_ID])
ORDER BY created_date DESC;
```

---

## 3️⃣ PRODUCT DETAIL TESTS

### Test 3.1: View Product Detail
**Steps:**
1. Click vào sản phẩm bất kỳ
2. URL: `http://localhost:8080/watches/{id}`

**Expected:**
- ✅ Hiển thị đầy đủ:
  - Product name
  - Brand name
  - Category
  - Price (gạch ngang nếu có discount)
  - Price after discount (màu đỏ, lớn)
  - Discount badge (nếu có)
  - Stock status (Còn hàng/Hết hàng)
  - Description
  - Specifications
- ✅ Image gallery: main image + thumbnails
- ✅ Quantity selector: +/- buttons
- ✅ "Thêm vào giỏ hàng" button
- ✅ Related Products section (4 products)

---

### Test 3.2: Image Gallery
**Steps:**
1. Click vào thumbnail image thứ 2

**Expected:**
- ✅ Main image đổi thành image thứ 2
- ✅ Thumbnail active state thay đổi

---

### Test 3.3: Quantity Selector
**Steps:**
1. Click nút "+" 3 lần
2. Click nút "-" 1 lần

**Expected:**
- ✅ Quantity: 1 → 4 → 3
- ✅ Không thể giảm < 1
- ✅ Không thể tăng > stock quantity

---

### Test 3.4: Add to Cart with Quantity
**Steps:**
1. Set quantity = 2
2. Click "Thêm vào giỏ hàng"

**Expected:**
- ✅ Notification thành công
- ✅ Cart badge tăng +2

**SQL Verify:**
```sql
SELECT * FROM CartItems 
WHERE watch_id = [WATCH_ID]
AND cart_id = (SELECT cart_id FROM Carts WHERE user_id = [USER_ID]);
-- quantity = 2
```

---

### Test 3.5: Related Products
**Steps:**
1. Scroll xuống "Sản phẩm liên quan"

**Expected:**
- ✅ Hiển thị 4 sản phẩm cùng category hoặc brand
- ✅ Không hiển thị sản phẩm hiện tại

**SQL Verify:**
```sql
SELECT TOP 4 * FROM Watches
WHERE category_id = [CATEGORY_ID]
AND watch_id != [CURRENT_WATCH_ID]
AND is_active = 1;
```

---

## 4️⃣ AUTHENTICATION TESTS

### Test 4.1: Register New User
**Steps:**
1. Truy cập `http://localhost:8080/register`
2. Điền form:
   - Username: testuser123
   - Email: test@example.com
   - Password: password123
   - Confirm Password: password123
   - Full Name: Test User
   - Phone: 0123456789
3. Click "Đăng ký"

**Expected:**
- ✅ Success message: "Đăng ký thành công! Vui lòng check email để xác thực tài khoản."
- ✅ Redirect to `/login`

**SQL Verify:**
```sql
SELECT * FROM Users WHERE email = 'test@example.com';
-- is_verified = 0
-- is_active = 1

SELECT * FROM VerificationTokens WHERE user_id = [USER_ID];
-- token exists, expiry_date = now + 24h
```

**Email Verify:**
- ✅ Check inbox: email "Xác thực tài khoản BOIZ SHOP"
- ✅ Email chứa link verify: `http://localhost:8080/verify?token={token}`

---

### Test 4.2: Email Verification
**Steps:**
1. Click vào link trong email
2. URL: `http://localhost:8080/verify?token={token}`

**Expected:**
- ✅ Success message: "Email đã được xác thực thành công!"
- ✅ Redirect to `/login`

**SQL Verify:**
```sql
SELECT * FROM Users WHERE email = 'test@example.com';
-- is_verified = 1

SELECT * FROM VerificationTokens WHERE user_id = [USER_ID];
-- Token đã bị xóa (hoặc used = 1)
```

---

### Test 4.3: Login
**Steps:**
1. Truy cập `http://localhost:8080/login`
2. Nhập:
   - Username: testuser123
   - Password: password123
3. Check "Remember me"
4. Click "Đăng nhập"

**Expected:**
- ✅ Login thành công
- ✅ Redirect to homepage
- ✅ Header hiển thị: "Xin chào, Test User" với dropdown menu
- ✅ Cookie JSESSIONID được tạo

**SQL Verify:**
```sql
SELECT * FROM Users WHERE username = 'testuser123';
-- Verify user exists và is_active = 1
```

---

### Test 4.4: Login Failed - Wrong Password
**Steps:**
1. Login với password sai

**Expected:**
- ✅ Error message: "Sai tên đăng nhập hoặc mật khẩu"
- ✅ Không redirect

---

### Test 4.5: Login Failed - Unverified Email
**Steps:**
1. Register user mới nhưng không verify email
2. Login

**Expected:**
- ✅ Error message: "Vui lòng xác thực email trước khi đăng nhập"

---

### Test 4.6: Logout
**Steps:**
1. Đã login
2. Click vào dropdown menu → "Đăng xuất"

**Expected:**
- ✅ Logout thành công
- ✅ Redirect to homepage
- ✅ Header hiển thị lại "Đăng nhập" + "Đăng ký"

---

## 5️⃣ SHOPPING CART TESTS

### Test 5.1: View Empty Cart
**Steps:**
1. Login
2. Truy cập `http://localhost:8080/cart`
3. Giỏ hàng đang trống

**Expected:**
- ✅ Hiển thị empty state: "Giỏ hàng trống"
- ✅ Button "Tiếp tục mua sắm"

---

### Test 5.2: Add Multiple Products to Cart
**Steps:**
1. Add 3 sản phẩm khác nhau vào cart

**Expected:**
- ✅ Cart badge = 3
- ✅ Truy cập `/cart` → hiển thị 3 sản phẩm

**SQL Verify:**
```sql
SELECT * FROM CartItems 
WHERE cart_id = (SELECT cart_id FROM Carts WHERE user_id = [USER_ID]);
-- 3 rows
```

---

### Test 5.3: Update Quantity in Cart
**Steps:**
1. Trong cart page, click nút "+" trên item 1
2. Đợi AJAX complete

**Expected:**
- ✅ Quantity tăng +1
- ✅ Subtotal của item update
- ✅ Total amount update
- ✅ Không reload page

**SQL Verify:**
```sql
SELECT quantity FROM CartItems WHERE cart_item_id = [ITEM_ID];
-- quantity increased
```

---

### Test 5.4: Remove Item from Cart
**Steps:**
1. Click nút "Xóa" trên item
2. Confirm dialog → "OK"

**Expected:**
- ✅ Item biến mất khỏi cart
- ✅ Cart badge giảm
- ✅ Total amount update

**SQL Verify:**
```sql
SELECT * FROM CartItems WHERE cart_item_id = [ITEM_ID];
-- Không còn (deleted)
```

---

### Test 5.5: Cart Summary Calculation
**Steps:**
1. Cart có 2 items:
   - Item 1: 1,000,000₫ x 1 = 1,000,000₫
   - Item 2: 500,000₫ x 2 = 1,000,000₫
2. Kiểm tra summary

**Expected:**
- ✅ Tạm tính: 2,000,000₫
- ✅ Phí vận chuyển: Miễn phí (vì >= 500,000₫)
- ✅ Tổng cộng: 2,000,000₫

---

### Test 5.6: Shipping Fee Calculation
**Test Case 1: Subtotal < 500,000₫**
```
Tạm tính: 300,000₫
Phí ship: 30,000₫
Tổng: 330,000₫
```

**Test Case 2: Subtotal >= 500,000₫**
```
Tạm tính: 500,000₫
Phí ship: Miễn phí
Tổng: 500,000₫
```

---

## 6️⃣ CHECKOUT & ORDER TESTS

### Test 6.1: Checkout Form
**Steps:**
1. Trong cart page, click "Tiến hành thanh toán"
2. Redirect to `/checkout`

**Expected:**
- ✅ Form hiển thị:
  - Receiver name (pre-filled với user.fullName)
  - Phone (pre-filled với user.phone)
  - Address (textarea)
  - Notes (textarea, optional)
- ✅ Payment method options:
  - COD (default selected)
  - VNPay
- ✅ Order summary hiển thị bên phải:
  - Product list
  - Subtotal
  - Shipping fee
  - Total

---

### Test 6.2: Submit Order - COD
**Steps:**
1. Điền form:
   - Receiver: Test User
   - Phone: 0123456789
   - Address: 123 ABC Street, District 1, HCM
   - Notes: Giao giờ hành chính
2. Select payment: COD
3. Click "Đặt hàng"

**Expected:**
- ✅ Order created successfully
- ✅ Redirect to `/order-success?orderId={id}`
- ✅ Cart cleared (cart badge = 0)

**SQL Verify:**
```sql
-- Check order created
SELECT * FROM Orders WHERE user_id = [USER_ID] ORDER BY order_date DESC;
-- order_status = 'PENDING'
-- payment_method_id = 1 (COD)

-- Check order details
SELECT * FROM OrderDetails WHERE order_id = [ORDER_ID];
-- Số lượng rows = số items trong cart
-- unit_price = giá GỐC
-- discount_amount = số tiền giảm per unit
-- subtotal = (unit_price - discount_amount) * quantity

-- Check stock updated
SELECT stock_quantity, sold_count FROM Watches WHERE watch_id = [WATCH_ID];
-- stock_quantity decreased
-- sold_count increased

-- Check cart cleared
SELECT * FROM CartItems WHERE cart_id = (SELECT cart_id FROM Carts WHERE user_id = [USER_ID]);
-- 0 rows

-- Check payment transaction created
SELECT * FROM PaymentTransactions WHERE order_id = [ORDER_ID];
-- status = 'PENDING'
```

**Email Verify:**
- ✅ Check inbox: email "[BOIZ SHOP] Xác nhận đơn hàng #ORD000001"
- ✅ Email hiển thị đầy đủ:
  - Order info
  - Product table với prices
  - Summary
  - Shipping info

---

### Test 6.3: Submit Order - VNPay (Nếu đã setup)
**Steps:**
1. Select payment: VNPay
2. Click "Đặt hàng"

**Expected:**
- ✅ Order created với status = 'PENDING'
- ✅ Redirect to VNPay payment page

**VNPay Test:**
1. Login với thẻ test: 9704198526191432198
2. OTP: 123456
3. Confirm payment

**Expected After Payment:**
- ✅ Redirect to `/payment/vnpay-return`
- ✅ Payment result page hiển thị:
  - Success icon
  - Order ID
  - Amount paid
  - Transaction code
  - Payment date

**SQL Verify:**
```sql
-- Order status updated
SELECT order_status FROM Orders WHERE order_id = [ORDER_ID];
-- 'CONFIRMED'

-- Payment transaction updated
SELECT * FROM PaymentTransactions WHERE order_id = [ORDER_ID];
-- status = 'SUCCESS'
-- transaction_code = VNPay transaction ID
```

---

## 7️⃣ ORDER MANAGEMENT TESTS

### Test 7.1: View Order History
**Steps:**
1. Login
2. Click dropdown menu → "Đơn hàng của tôi"
3. Hoặc truy cập `/user/orders`

**Expected:**
- ✅ List all orders của user
- ✅ Mỗi order hiển thị:
  - Order ID (ORD000001)
  - Order date
  - Total amount
  - Status badge (màu theo status)
  - "Xem chi tiết" button

**SQL Verify:**
```sql
SELECT * FROM Orders WHERE user_id = [USER_ID] ORDER BY order_date DESC;
```

---

### Test 7.2: View Order Detail
**Steps:**
1. Click "Xem chi tiết" trên order

**Expected:**
- ✅ Modal/Page hiển thị:
  - Order info
  - Product list với prices
  - Summary (subtotal, shipping, total)
  - Shipping info
  - Payment method
  - Order status

---

### Test 7.3: Cancel Order
**Steps:**
1. Order status = PENDING
2. Click "Hủy đơn hàng"
3. Confirm

**Expected:**
- ✅ Order status → CANCELLED
- ✅ Stock restored

**SQL Verify:**
```sql
-- Order cancelled
SELECT order_status FROM Orders WHERE order_id = [ORDER_ID];
-- 'CANCELLED'

-- Stock restored
SELECT stock_quantity, sold_count FROM Watches WHERE watch_id = [WATCH_ID];
-- stock_quantity increased back
-- sold_count decreased back
```

---

### Test 7.4: Cannot Cancel Non-PENDING Order
**Steps:**
1. Order status = CONFIRMED/SHIPPING/DELIVERED
2. Không có button "Hủy đơn hàng"

**Expected:**
- ✅ Button không hiển thị
- ✅ Hoặc hiển thị disabled

---

## 8️⃣ USER PROFILE TESTS

### Test 8.1: View Profile
**Steps:**
1. Login
2. Click "Tài khoản của tôi"
3. Hoặc truy cập `/user/profile`

**Expected:**
- ✅ Hiển thị thông tin:
  - Username (readonly)
  - Email (readonly)
  - Full Name
  - Phone
  - Created Date

---

### Test 8.2: Update Profile
**Steps:**
1. Sửa Full Name: "New Name"
2. Sửa Phone: "0987654321"
3. Click "Cập nhật"

**Expected:**
- ✅ Success message
- ✅ Profile updated

**SQL Verify:**
```sql
SELECT full_name, phone FROM Users WHERE user_id = [USER_ID];
-- Updated values
```

---

### Test 8.3: Change Password
**Steps:**
1. Tab "Đổi mật khẩu"
2. Nhập:
   - Current Password: password123
   - New Password: newpassword123
   - Confirm: newpassword123
3. Click "Đổi mật khẩu"

**Expected:**
- ✅ Success message
- ✅ Password updated (BCrypt)
- ✅ Can login with new password

**SQL Verify:**
```sql
SELECT password_hash FROM Users WHERE user_id = [USER_ID];
-- Hash changed
```

---

## 9️⃣ RESPONSIVE TESTS

### Test 9.1: Mobile View (< 768px)
**Steps:**
1. Resize browser to 375px width
2. Hoặc dùng Chrome DevTools mobile emulation

**Expected:**
- ✅ Hamburger menu thay navigation bar
- ✅ Product grid: 1 column
- ✅ Cart page: vertical layout
- ✅ All buttons touch-friendly (min 44px)

---

### Test 9.2: Tablet View (768px - 1199px)
**Expected:**
- ✅ Product grid: 2 columns
- ✅ Navigation bar collapse

---

### Test 9.3: Desktop View (>= 1200px)
**Expected:**
- ✅ Product grid: 3 columns
- ✅ Full navigation bar

---

## 🔟 PERFORMANCE TESTS

### Test 10.1: Page Load Time
**Steps:**
1. Chrome DevTools → Network tab
2. Reload homepage

**Expected:**
- ✅ Page load < 3 seconds
- ✅ No 404 errors
- ✅ All images loaded

---

### Test 10.2: AJAX Performance
**Steps:**
1. Add to cart 10 times nhanh liên tiếp

**Expected:**
- ✅ No errors
- ✅ Cart badge update correctly
- ✅ No duplicate cart items

---

## 📊 SQL VERIFICATION QUERIES

### Check Database State
```sql
-- Users
SELECT COUNT(*) AS total_users FROM Users;
SELECT COUNT(*) AS verified_users FROM Users WHERE is_verified = 1;

-- Products
SELECT COUNT(*) AS total_products FROM Watches;
SELECT COUNT(*) AS active_products FROM Watches WHERE is_active = 1;
SELECT COUNT(*) AS out_of_stock FROM Watches WHERE stock_quantity = 0;

-- Orders
SELECT COUNT(*) AS total_orders FROM Orders;
SELECT order_status, COUNT(*) AS count 
FROM Orders 
GROUP BY order_status;

-- Revenue
SELECT SUM(total_amount) AS total_revenue 
FROM Orders 
WHERE order_status IN ('DELIVERED', 'COMPLETED');

-- Carts
SELECT COUNT(*) AS active_carts FROM Carts;
SELECT COUNT(*) AS total_cart_items FROM CartItems;
```

---

## 🐛 COMMON ISSUES

### Issue 1: Email không gửi được
**Check:**
```properties
# application.properties
spring.mail.host=smtp.gmail.com
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```
**Fix:** Tạo Gmail App Password

---

### Issue 2: Cart badge không update
**Check:**
- AJAX endpoint: `/cart/count`
- JavaScript console có errors không
**Fix:** Check `main.js` updateCartBadge()

---

### Issue 3: Stock không update sau order
**Check OrderService.java:**
```java
watch.setStockQuantity(watch.getStockQuantity() - item.getQuantity());
watchRepository.save(watch);
```

---

### Issue 4: VNPay redirect lỗi
**Check:**
- VNPay TMN Code và Hash Secret đúng chưa
- Return URL match với config trong VNPay

---

## ✅ TEST COMPLETION CHECKLIST

### User Features (Must Test)
- [ ] Homepage load với 3 sections
- [ ] Product listing với search/filter
- [ ] Product detail với related products
- [ ] Register → Email verification → Login
- [ ] Add to cart (AJAX)
- [ ] Update/remove cart items
- [ ] Checkout COD
- [ ] Order confirmation email received
- [ ] View order history
- [ ] Cancel order
- [ ] Update profile
- [ ] Change password

### Optional Tests
- [ ] OAuth2 login (nếu setup)
- [ ] VNPay payment (nếu setup)
- [ ] Responsive mobile/tablet
- [ ] Performance testing

---

## 🎓 TESTING TIPS FOR INTERN

1. **Test từ user perspective:** Đứng góc độ khách hàng, flow có smooth không?
2. **Check database sau mỗi action:** Verify data đúng chưa
3. **Test error cases:** Không chỉ test happy path
4. **Use Chrome DevTools:** Network tab, Console tab để debug
5. **Test responsive:** Resize browser window
6. **Clear cache thường xuyên:** Ctrl + Shift + Delete
7. **Test với multiple users:** Mở incognito window
8. **Document bugs:** Screenshot + steps to reproduce

---

**🎊 Happy Testing! Nếu gặp bug, check PENDING_TASKS.md để xem có liên quan đến 35% chưa hoàn thành không! 🎊**
