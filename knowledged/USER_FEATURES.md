# 👤 CHỨC NĂNG USER - 2BSHOP

> **Tài liệu này mô tả chi tiết tất cả chức năng mà người dùng (USER) có thể sử dụng trong hệ thống 2BShop**

---

## 📋 MỤC LỤC

1. [Authentication - Xác thực](#1-authentication---xác-thực)
2. [Product Browsing - Xem sản phẩm](#2-product-browsing---xem-sản-phẩm)
3. [Shopping Cart - Giỏ hàng](#3-shopping-cart---giỏ-hàng)
4. [Checkout - Thanh toán](#4-checkout---thanh-toán)
5. [Order Management - Quản lý đơn hàng](#5-order-management---quản-lý-đơn-hàng)
6. [User Profile - Quản lý tài khoản](#6-user-profile---quản-lý-tài-khoản)
7. [Payment Methods - Phương thức thanh toán](#7-payment-methods---phương-thức-thanh-toán)
8. [Invoice - Hóa đơn](#8-invoice---hóa-đơn)

---

## 1. AUTHENTICATION - XÁC THỰC

### 1.1. Đăng ký tài khoản

**📌 Mục đích:** Tạo tài khoản mới cho người dùng

**🔗 Files liên quan:**
- **Controller:** `PublicController.java`
  - Method: `registerPage()` - GET `/register` (hiển thị form)
  - Method: `processRegister()` - POST `/register` (xử lý đăng ký)
- **Service:** `UserService.java`
  - Method: `registerUser(RegisterDTO dto)`
  - Method: `existsByEmail(String email)`
- **Template:** `templates/public/register.html`
- **DTO:** `RegisterDTO.java` (chứa: username, email, password, fullName, phone)

**🎯 Model Attributes (UI):**
```java
model.addAttribute("registerDTO", new RegisterDTO());  // Form binding
model.addAttribute("success", "Đăng ký thành công!");   // Flash message
model.addAttribute("error", "Email đã được sử dụng!");  // Error message
```

**⚙️ Luồng xử lý:**
1. User điền form → Submit POST `/register`
2. `PublicController.processRegister()` nhận RegisterDTO
3. Validate email tồn tại → `userService.existsByEmail()`
4. Tạo user mới → `userService.registerUser(dto)`
5. Gửi email xác thực → `mailService.sendVerificationEmail()`
6. Redirect về `/login` với thông báo thành công

---

### 1.2. Xác thực email (Email Verification)

**📌 Mục đích:** Kích hoạt tài khoản sau khi đăng ký

**🔗 Files liên quan:**
- **Controller:** `PublicController.java`
  - Method: `verifyEmail()` - GET `/verify`
  - Method: `resendVerification()` - POST `/resend-verification`
- **Service:** `UserService.java`
  - Method: `verifyEmail(String email, String token)`
- **Template:** `templates/public/verify-email.html`

**🎯 Model Attributes:**
```java
@RequestParam String email   // Email cần xác thực
@RequestParam String token   // Token từ email
model.addAttribute("success", "Xác thực thành công!")
```

**⚙️ Luồng xử lý:**
1. User click link trong email → GET `/verify?email=...&token=...`
2. `PublicController.verifyEmail()` gọi `userService.verifyEmail()`
3. Kiểm tra token hợp lệ → Set `isEnabled = true`
4. Redirect về `/login` với thông báo

---

### 1.3. Đăng nhập (Form Login)

**📌 Mục đích:** Xác thực và tạo session cho user bằng email/password

**🔗 Files liên quan:**
- **Controller:** `PublicController.java`
  - Method: `loginPage()` - GET `/login`
- **Security:** `SecurityConfig.java`
  - Form login URL: `/perform-login`
  - Success Handler: `CustomLoginSuccessHandler.java`
- **Template:** `templates/public/login.html`

**🎯 Model Attributes:**
```java
@RequestParam(required = false) String error   // Login thất bại
@RequestParam(required = false) String logout  // Đăng xuất thành công
model.addAttribute("error", "Email hoặc mật khẩu không đúng!")
```

**⚙️ Luồng xử lý:**
1. User nhập email/password → POST `/perform-login`
2. Spring Security xử lý authentication
3. Success → Redirect về `/` (homepage) hoặc trang trước đó
4. Failure → Redirect về `/login?error=true`

---

### 1.4. Đăng nhập bằng Google (OAuth2 Login)

**📌 Mục đích:** Xác thực nhanh bằng tài khoản Google, không cần password

**🔗 Files liên quan:**
- **Controller:** `PublicController.java`
  - Method: `loginPage()` - GET `/login` (hiển thị nút Google)
- **Security:** `SecurityConfig.java`
  - OAuth2 login configuration
  - Success Handler: `CustomLoginSuccessHandler.java`
- **Service:** `CustomOAuth2UserService.java`
  - Method: `loadUser()` - Xử lý OAuth2 authentication
  - Method: `processOAuthUser()` - Tạo/cập nhật user từ Google account
- **Service:** `PhoneVerificationService.java`
  - Method: `needsPhoneVerification()` - Kiểm tra user OAuth2 cần verify phone
  - Method: `updatePhoneAndVerify()` - Cập nhật số điện thoại
- **Entity:** `CustomOAuth2User.java` - Wrapper cho OAuth2User
- **Template:** `templates/public/login.html`
- **Template:** `templates/user/profile.html` (phone verification UI)

**🎯 Model Attributes:**
```java
// Login page
model.addAttribute("oauth2Enabled", true)  // Hiển thị nút Google login

// Profile page (sau khi login OAuth2)
model.addAttribute("requirePhone", boolean)  // Yêu cầu cập nhật phone
model.addAttribute("profile", UserProfileDTO)  // profile.provider = "GOOGLE"
```

**🎯 Database Fields (User entity):**
```java
provider VARCHAR(20)      // "LOCAL" hoặc "GOOGLE"
provider_id VARCHAR(100)  // Google user ID (sub)
email_verified BIT        // true (Google đã verify)
phone_verified BIT        // false (cần user cập nhật)
password VARCHAR(255)     // NULL (OAuth2 không dùng password)
avatar_url VARCHAR(255)   // Google profile picture URL
```

**⚙️ Luồng xử lý đăng nhập:**
1. User click nút "Google" trên trang login → Redirect `/oauth2/authorization/google`
2. Spring Security redirect sang Google OAuth2 consent screen
3. User đăng nhập Google và cho phép ứng dụng truy cập email/profile
4. Google callback về `/login/oauth2/code/google` với authorization code
5. Spring Security tự động:
   - Exchange code lấy access token
   - Gọi Google UserInfo API lấy thông tin user
6. `CustomOAuth2UserService.loadUser()` được gọi:
   - Extract email, name, providerId, avatarUrl từ Google response
   - Gọi `processOAuthUser()` để xử lý
7. `processOAuthUser()` logic:
   - **Nếu email đã tồn tại**: 
     - Cập nhật provider = "GOOGLE", providerId
     - Set emailVerified = true
     - Set isEnabled = true
     - Giữ nguyên phone nếu đã có
     - Update avatarUrl nếu chưa có
   - **Nếu user mới**:
     - Tạo User entity mới
     - Set provider = "GOOGLE", emailVerified = true
     - Set phoneVerified = false (YÊU CẦU cập nhật sau)
     - Set password = NULL (không cần password)
     - Tạo username từ email
     - Tạo UserRole với role = "USER"
8. Return `CustomOAuth2User` chứa User entity + authorities
9. `CustomLoginSuccessHandler` redirect về homepage
10. User đã đăng nhập thành công với session

**⚙️ Luồng xử lý Phone Verification (Bắt buộc cho OAuth2):**
1. User OAuth2 login thành công → Browse sản phẩm, thêm vào cart → OK
2. User click "Thanh toán" → GET `/checkout`
3. `CheckoutController.checkoutPage()`:
   - Kiểm tra `phoneVerificationService.needsPhoneVerification(user)`
   - **Nếu true** (provider = GOOGLE và phone chưa verify):
     - Set flash attribute: `requirePhone = true`
     - Redirect về `/profile` với thông báo lỗi
4. Profile page hiển thị warning alert:
   ```html
   <div th:if="${requirePhone}" class="alert alert-warning">
     <h4>Yêu cầu cập nhật số điện thoại</h4>
     <p>Để đảm bảo giao hàng chính xác, vui lòng cập nhật số điện thoại trước khi đặt hàng.</p>
   </div>
   ```
5. User nhập số điện thoại → POST `/profile/update-phone`
6. `phoneVerificationService.updatePhoneAndVerify()`:
   - Validate phone format (Regex: `^(0|84)(3|5|7|8|9)[0-9]{8}$`)
   - Set user.phone = phone
   - Set user.phoneVerified = true
   - Save to database
7. Redirect về `/checkout` → Checkout được phép tiếp tục

**🎯 Business Rules:**
- OAuth2 users **KHÔNG CẦN** xác thực email (Google đã verify)
- OAuth2 users **BẮT BUỘC** phải cập nhật số điện thoại trước khi checkout
- OAuth2 users **KHÔNG** cần password (không thể đổi password)
- Nếu email đã tồn tại với account LOCAL → merge thành 1 account, set provider = GOOGLE
- Avatar tự động lấy từ Google profile picture

**🎯 Security Configuration (application.properties):**
```properties
# Google OAuth2 Configuration
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=email,profile
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/google

# Google Provider
spring.security.oauth2.client.provider.google.authorization-uri=https://accounts.google.com/o/oauth2/v2/auth
spring.security.oauth2.client.provider.google.token-uri=https://oauth2.googleapis.com/token
spring.security.oauth2.client.provider.google.user-info-uri=https://www.googleapis.com/oauth2/v3/userinfo
spring.security.oauth2.client.provider.google.user-name-attribute=sub
```

**🎯 API Mapping (Google UserInfo Response):**
```java
// Google returns:
{
  "sub": "103456789012345678901",        // providerId
  "name": "Nguyễn Văn A",                // fullName
  "email": "example@gmail.com",          // email
  "picture": "https://lh3.googleusercontent.com/..." // avatarUrl
}

// We map to:
user.setProviderId(attributes.get("sub"))
user.setFullName(attributes.get("name"))
user.setEmail(attributes.get("email"))
user.setAvatarUrl(attributes.get("picture"))
```

**🔒 Authorization:**
- OAuth2 users có đầy đủ quyền như LOCAL users
- Role: `ROLE_USER` (tự động gán khi tạo account mới)
- getAuthorities() trả về từ UserRoles trong database

**📧 Email Notifications:**
- **Không** gửi email xác thực (Google đã verify)
- Gửi email chào mừng sau khi OAuth2 login lần đầu (optional)
- Gửi email order confirmation như bình thường

**🐛 Common Issues & Solutions:**
1. **"User: NULL" khi truy cập /profile**
   - **Nguyên nhân**: Controller dùng `@AuthenticationPrincipal UserDetails` không support OAuth2User
   - **Fix**: Đổi thành `Principal principal` và extract email với helper method
   
2. **Bị redirect về /login khi đã OAuth2 login**
   - **Nguyên nhân**: UserRole không được save vào database → getAuthorities() rỗng
   - **Fix**: Save UserRole entity và reload user với authorities

3. **NullPointerException trên Boolean fields**
   - **Nguyên nhân**: Existing users có NULL trong emailVerified/phoneVerified
   - **Fix**: Dùng `Boolean.TRUE.equals(user.getEmailVerified())` thay vì `.getEmailVerified()`

---

### 1.5. Quên mật khẩu (Password Reset)

**📌 Mục đích:** Khôi phục mật khẩu khi user quên

**🔗 Files liên quan:**
- **Controller:** `PasswordResetController.java`
  - Method: `showForgotPasswordForm()` - GET `/forgot-password`
  - Method: `processForgotPassword()` - POST `/forgot-password`
  - Method: `showResetPasswordForm()` - GET `/reset-password`
  - Method: `processResetPassword()` - POST `/reset-password`
- **Service:** `PasswordResetService.java`
  - Method: `createResetToken(String email)`
  - Method: `validateResetToken(String token)`
  - Method: `resetPassword(String token, String newPassword)`
- **Templates:**
  - `templates/auth/forgot-password.html`
  - `templates/auth/reset-password.html`

**🎯 Model Attributes:**
```java
@RequestParam String email       // Email để reset
@RequestParam String token       // Token từ email
@RequestParam String password    // Mật khẩu mới
model.addAttribute("success", "Email đã được gửi!")
```

**⚙️ Luồng xử lý:**
1. User nhập email → POST `/forgot-password`
2. `passwordResetService.createResetToken()` tạo token
3. Gửi email chứa link reset → `mailService.sendPasswordResetEmail()`
4. User click link → GET `/reset-password?token=...`
5. Nhập mật khẩu mới → POST `/reset-password`
6. `passwordResetService.resetPassword()` cập nhật DB

---

## 2. PRODUCT BROWSING - XEM SẢN PHẨM

### 2.1. Trang chủ - Homepage

**📌 Mục đích:** Hiển thị sản phẩm nổi bật, mới nhất

**🔗 Files liên quan:**
- **Controller:** `PublicController.java`
  - Method: `homePage()` - GET `/`
- **Service:** `WatchService.java`
  - Method: `getFeaturedWatches()`
  - Method: `getNewestWatches()`
- **Template:** `templates/public/index.html`

**🎯 Model Attributes:**
```java
model.addAttribute("featuredWatches", List<Watch>)  // Sản phẩm nổi bật
model.addAttribute("newestWatches", List<Watch>)    // Sản phẩm mới
```

**⚙️ Luồng xử lý:**
1. User truy cập `/` 
2. `watchService.getFeaturedWatches()` lấy top sản phẩm bán chạy
3. `watchService.getNewestWatches()` lấy sản phẩm mới
4. Render `index.html` với danh sách sản phẩm

---

### 2.2. Danh sách sản phẩm (Product Listing)

**📌 Mục đích:** Hiển thị tất cả sản phẩm với filter, search, pagination

**🔗 Files liên quan:**
- **Controller:** `PublicController.java`
  - Method: `allProducts()` - GET `/watches`
- **Service:** `WatchService.java`
  - Method: `searchWatches(search, brand, priceRange, page, size, sort)`
- **Template:** `templates/public/products.html`

**🎯 Model Attributes:**
```java
@RequestParam(required = false) String search      // Từ khóa tìm kiếm
@RequestParam(required = false) String brand       // Lọc theo thương hiệu
@RequestParam(required = false) String priceRange  // Khoảng giá
@RequestParam(required = false) String sort        // Sắp xếp (newest, price-asc, price-desc)
@RequestParam(defaultValue = "0") int page         // Trang hiện tại
@RequestParam(defaultValue = "6") int size         // Số sản phẩm/trang

model.addAttribute("watches", Page<Watch>)          // Danh sách sản phẩm (paging)
model.addAttribute("brands", List<WatchBrand>)      // Danh sách brand để filter
model.addAttribute("currentPage", int)
model.addAttribute("totalPages", int)
model.addAttribute("search", String)                // Giữ lại từ khóa search
model.addAttribute("selectedBrand", String)
model.addAttribute("selectedPriceRange", String)
```

**⚙️ Luồng xử lý:**
1. User truy cập `/watches?search=rolex&brand=1&priceRange=5-10&sort=price-asc&page=0`
2. `watchService.searchWatches()` query database với filters
3. Return `Page<Watch>` với pagination
4. Render `products.html` với danh sách và filter options

---

### 2.3. Chi tiết sản phẩm (Product Detail)

**📌 Mục đích:** Hiển thị thông tin chi tiết 1 sản phẩm

**🔗 Files liên quan:**
- **Controller:** `PublicController.java`
  - Method: `productDetail()` - GET `/watches/{id}`
- **Service:** `WatchService.java`
  - Method: `findById(Integer id)`
  - Method: `getRelatedWatches(Integer watchId)`
- **Template:** `templates/public/product-detail.html`

**🎯 Model Attributes:**
```java
@PathVariable Integer id  // ID sản phẩm

model.addAttribute("watch", Watch)                    // Sản phẩm chính
model.addAttribute("relatedWatches", List<Watch>)     // Sản phẩm liên quan
model.addAttribute("images", List<String>)            // Danh sách hình ảnh
```

**⚙️ Luồng xử lý:**
1. User click vào sản phẩm → GET `/watches/5`
2. `watchService.findById(5)` lấy thông tin Watch
3. `watchService.getRelatedWatches(5)` lấy sản phẩm cùng brand
4. Render `product-detail.html` với thông tin chi tiết

---

### 2.4. Sản phẩm giảm giá

**📌 Mục đích:** Hiển thị sản phẩm đang có discount

**🔗 Files liên quan:**
- **Controller:** `PublicController.java`
  - Method: `discountProducts()` - GET `/watches/discount`
- **Service:** `WatchService.java`
  - Method: `getDiscountedWatches(page, size)`
- **Template:** `templates/public/products.html` (reuse)

**🎯 Model Attributes:**
```java
model.addAttribute("watches", Page<Watch>)  // Chỉ sản phẩm có discountPercent > 0
model.addAttribute("pageTitle", "Sản phẩm giảm giá")
```

---

### 2.5. Sản phẩm mới nhất

**📌 Mục đích:** Hiển thị sản phẩm mới thêm vào hệ thống

**🔗 Files liên quan:**
- **Controller:** `PublicController.java`
  - Method: `newestProducts()` - GET `/watches/newest`
- **Service:** `WatchService.java`
  - Method: `getNewestWatches(page, size)`
- **Template:** `templates/public/products.html`

**🎯 Model Attributes:**
```java
model.addAttribute("watches", Page<Watch>)  // Sắp xếp theo createdDate DESC
```

---

## 3. SHOPPING CART - GIỎ HÀNG

### 3.1. Xem giỏ hàng

**📌 Mục đích:** Hiển thị danh sách sản phẩm trong giỏ

**🔗 Files liên quan:**
- **Controller:** `CartController.java`
  - Method: `viewCart()` - GET `/cart`
- **Service:** `CartService.java`
  - Method: `getCartItems()`
  - Method: `getCartTotal()`
- **Template:** `templates/user/cart.html`

**🎯 Model Attributes:**
```java
model.addAttribute("cartItems", List<CartItem>)  // Danh sách sản phẩm trong cart
model.addAttribute("cartTotal", BigDecimal)      // Tổng tiền
model.addAttribute("itemCount", int)             // Số lượng items
```

**⚙️ Luồng xử lý:**
1. User truy cập `/cart`
2. `cartService.getCartItems()` lấy cart của user hiện tại
3. Tính tổng tiền → `cartService.getCartTotal()`
4. Render `cart.html` với danh sách items

---

### 3.2. Thêm vào giỏ hàng

**📌 Mục đích:** Thêm sản phẩm vào cart

**🔗 Files liên quan:**
- **Controller:** `CartController.java`
  - Method: `addToCart()` - POST `/cart/add`
- **Service:** `CartService.java`
  - Method: `addToCart(Integer watchId, int quantity)`
- **Template:** JavaScript AJAX call

**🎯 Request Parameters:**
```java
@RequestParam Integer watchId   // ID sản phẩm
@RequestParam int quantity      // Số lượng
```

**🎯 Response (JSON):**
```json
{
  "success": true,
  "message": "Đã thêm vào giỏ hàng",
  "cartItemCount": 3
}
```

**⚙️ Luồng xử lý:**
1. User click "Thêm vào giỏ" → AJAX POST `/cart/add`
2. `cartService.addToCart()` kiểm tra stock
3. Nếu sản phẩm đã có → tăng quantity
4. Nếu chưa có → tạo CartItem mới
5. Return JSON response

---

### 3.3. Cập nhật số lượng

**📌 Mục đích:** Thay đổi quantity của item trong cart

**🔗 Files liên quan:**
- **Controller:** `CartController.java`
  - Method: `updateCart()` - POST `/cart/update`
- **Service:** `CartService.java`
  - Method: `updateQuantity(Integer cartItemId, int quantity)`
- **Template:** JavaScript AJAX

**🎯 Request Parameters:**
```java
@RequestParam Integer cartItemId  // ID của CartItem
@RequestParam int quantity        // Số lượng mới
```

**🎯 Response (JSON):**
```json
{
  "success": true,
  "newSubtotal": 15000000,
  "cartTotal": 30000000
}
```

**⚙️ Luồng xử lý:**
1. User thay đổi quantity → AJAX POST `/cart/update`
2. `cartService.updateQuantity()` cập nhật DB
3. Kiểm tra stock availability
4. Tính lại subtotal và total
5. Return JSON với giá mới

---

### 3.4. Xóa sản phẩm khỏi cart

**📌 Mục đích:** Loại bỏ item khỏi giỏ hàng

**🔗 Files liên quan:**
- **Controller:** `CartController.java`
  - Method: `removeFromCart()` - POST `/cart/remove`
- **Service:** `CartService.java`
  - Method: `removeItem(Integer cartItemId)`

**🎯 Request Parameters:**
```java
@RequestParam Integer cartItemId
```

**🎯 Response (JSON):**
```json
{
  "success": true,
  "message": "Đã xóa sản phẩm",
  "cartTotal": 15000000
}
```

---

### 3.5. Chọn sản phẩm để checkout

**📌 Mục đích:** Đánh dấu item để thanh toán (checkbox)

**🔗 Files liên quan:**
- **Controller:** `CartController.java`
  - Method: `selectItems()` - POST `/cart/select`
  - Method: `selectAll()` - POST `/cart/select-all`
- **Service:** `CartService.java`
  - Method: `selectItem(Integer cartItemId, boolean selected)`

**🎯 Request Parameters:**
```java
@RequestParam Integer cartItemId
@RequestParam boolean selected  // true/false
```

**⚙️ Luồng xử lý:**
1. User check/uncheck checkbox → AJAX POST `/cart/select`
2. `cartService.selectItem()` set `isSelected` trong DB
3. Tính lại total chỉ với items được chọn
4. Return JSON với total mới

---

### 3.6. Số lượng items trong cart (Badge)

**📌 Mục đích:** Hiển thị số items trong cart badge (header)

**🔗 Files liên quan:**
- **Controller:** `CartController.java`
  - Method: `getCartCount()` - GET `/cart/count`
- **Service:** `CartService.java`
  - Method: `getCartItemCount()`
- **Template:** `fragments/header.html`

**🎯 Model Attributes:**
```java
model.addAttribute("cartItemCount", int)  // Số lượng items trong cart
```

**🎯 Response (JSON):**
```json
{
  "count": 5
}
```

**⚙️ Luồng xử lý:**
1. Page load → AJAX GET `/cart/count`
2. `cartService.getCartItemCount()` đếm số items
3. Update badge trong header

---

## 4. CHECKOUT - THANH TOÁN

### 4.1. Trang checkout

**📌 Mục đích:** Hiển thị form thanh toán với thông tin giao hàng

**🔗 Files liên quan:**
- **Controller:** `CheckoutController.java`
  - Method: `checkoutPage()` - GET `/checkout`
- **Service:** `CheckoutService.java`
  - Method: `calculateTotals(String couponCode)`
- **Service:** `BankAccountService.java`
  - Method: `getActiveBankAccounts()`
- **Template:** `templates/public/checkout.html`

**🎯 Model Attributes:**
```java
model.addAttribute("cartItems", List<CartItem>)      // Items đã select
model.addAttribute("summary", CheckoutSummary)       // Tổng tiền, discount, shipping
model.addAttribute("user", User)                     // Thông tin user
model.addAttribute("bankAccounts", List<BankAccount>) // Danh sách bank để chuyển khoản
```

**⚙️ Luồng xử lý:**
1. User click "Thanh toán" từ cart → GET `/checkout`
2. `cartService.getSelectedCartItems()` lấy items đã chọn
3. `checkoutService.calculateTotals()` tính tổng tiền
4. `bankAccountService.getActiveBankAccounts()` lấy danh sách ngân hàng
5. Render form checkout với thông tin giao hàng

---

### 4.2. Đặt hàng (Place Order)

**📌 Mục đích:** Tạo đơn hàng mới và lưu vào DB

**🔗 Files liên quan:**
- **Controller:** `CheckoutController.java`
  - Method: `placeOrder()` - POST `/checkout/place-order`
- **Service:** `CheckoutService.java`
  - Method: `placeOrder(user, receiverName, phone, address, notes, paymentMethod, couponCode, bankAccountId)`
- **Service:** `MailService.java`
  - Method: `sendOrderConfirmation(Order order, List<OrderDetail> details)`
- **Template:** Redirect to `/user/orders`

**🎯 Request Parameters:**
```java
@RequestParam String receiverName     // Tên người nhận
@RequestParam String phone            // Số điện thoại
@RequestParam String address          // Địa chỉ giao hàng
@RequestParam String paymentMethod    // COD, VNPAY, BANK_TRANSFER
@RequestParam(required = false) String notes          // Ghi chú
@RequestParam(required = false) String couponCode     // Mã giảm giá
@RequestParam(required = false) Integer bankAccountId // ID ngân hàng (nếu chọn banking)
```

**⚙️ Luồng xử lý:**
1. User submit form → POST `/checkout/place-order`
2. `checkoutService.placeOrder()`:
   - Validate stock availability
   - Tạo Order entity
   - Tạo OrderDetail cho từng item
   - Giảm stock quantity
   - Tăng soldCount
   - Áp dụng coupon (nếu có)
   - Lưu vào orders, order_details table
3. `mailService.sendOrderConfirmation()` gửi email xác nhận
4. `cartService.clearCart()` xóa giỏ hàng
5. Redirect → `/user/orders` với flash message

---

### 4.3. Trang xác nhận đơn hàng

**📌 Mục đích:** Hiển thị thông báo đặt hàng thành công

**🔗 Files liên quan:**
- **Controller:** `CheckoutController.java`
  - Method: `orderConfirmation()` - GET `/checkout/confirmation/{orderId}`
- **Service:** `CheckoutService.java`
  - Method: `getOrderById(Integer orderId)`
- **Template:** `templates/public/order-confirmation.html`

**🎯 Model Attributes:**
```java
@PathVariable Integer orderId

model.addAttribute("order", Order)              // Thông tin đơn hàng
model.addAttribute("orderDetails", List<OrderDetail>)
```

---

## 5. ORDER MANAGEMENT - QUẢN LÝ ĐƠN HÀNG

### 5.1. Danh sách đơn hàng của user

**📌 Mục đích:** Hiển thị tất cả đơn hàng của user với filter theo status

**🔗 Files liên quan:**
- **Controller:** `UserController.java`
  - Method: `myOrders()` - GET `/user/orders`
- **Service:** `OrderService.java`
  - Method: `findByUserEmail(String email)`
- **Template:** `templates/user/my-orders.html`

**🎯 Model Attributes:**
```java
@RequestParam(required = false) String status  // PENDING, CONFIRMED, SHIPPING, DELIVERED, COMPLETED, CANCELLED
@RequestParam(defaultValue = "0") int page

model.addAttribute("orders", Page<Order>)      // Danh sách đơn hàng (paging)
model.addAttribute("totalPages", int)
model.addAttribute("currentPage", int)
model.addAttribute("selectedStatus", String)   // Giữ lại filter
```

**⚙️ Luồng xử lý:**
1. User click "Đơn hàng của tôi" → GET `/user/orders`
2. `orderService.findByUserEmail()` lấy orders của user
3. Filter theo status nếu có
4. Pagination manual (10 items/page)
5. Render `my-orders.html` với danh sách

---

### 5.2. Chi tiết đơn hàng

**📌 Mục đích:** Xem thông tin chi tiết 1 đơn hàng

**🔗 Files liên quan:**
- **Controller:** `UserController.java`
  - Method: `orderDetail()` - GET `/user/orders/{id}`
- **Service:** `OrderService.java`
  - Method: `findById(Integer orderId)`
- **Template:** `templates/user/order-detail.html`

**🎯 Model Attributes:**
```java
@PathVariable Integer id

model.addAttribute("order", Order)                   // Thông tin order
model.addAttribute("orderDetails", List<OrderDetail>) // Danh sách sản phẩm
model.addAttribute("canCancel", boolean)             // Có thể hủy hay không
```

**⚙️ Luồng xử lý:**
1. User click vào đơn hàng → GET `/user/orders/5`
2. `orderService.findById(5)` lấy Order
3. Kiểm tra ownership (order.user == current user)
4. Kiểm tra status → canCancel = (status == PENDING)
5. Render order detail

---

### 5.3. Hủy đơn hàng

**📌 Mục đích:** User tự hủy đơn hàng (chỉ khi status = PENDING)

**🔗 Files liên quan:**
- **Controller:** `UserController.java`
  - Method: `cancelOrder()` - POST `/user/orders/{id}/cancel`
- **Service:** `OrderService.java`
  - Method: `cancelOrder(Integer orderId)`
- **Service:** `MailService.java`
  - Method: `sendCancelledEmail()`

**🎯 Request Parameters:**
```java
@PathVariable Integer id
@RequestParam(required = false) String reason  // Lý do hủy
```

**⚙️ Luồng xử lý:**
1. User click "Hủy đơn hàng" → POST `/user/orders/5/cancel`
2. `orderService.cancelOrder(5)`:
   - Kiểm tra status = PENDING
   - Set orderStatus = CANCELLED
   - Hoàn lại stock quantity
   - Giảm soldCount
3. `mailService.sendCancelledEmail()` thông báo
4. Redirect về `/user/orders` với flash message

---

## 6. USER PROFILE - QUẢN LÝ TÀI KHOẢN

### 6.1. Xem thông tin tài khoản

**📌 Mục đích:** Hiển thị profile của user

**🔗 Files liên quan:**
- **Controller:** `UserProfileController.java`
  - Method: `profilePage()` - GET `/profile`
- **Service:** `UserProfileService.java`
  - Method: `getUserProfile(String email)`
- **Template:** `templates/user/profile.html`

**🎯 Model Attributes:**
```java
model.addAttribute("user", User)  // Thông tin user hiện tại
```

**⚙️ Luồng xử lý:**
1. User click "Tài khoản" → GET `/profile`
2. Lấy email từ SecurityContext
3. `userProfileService.getUserProfile()` lấy thông tin user
4. Render `profile.html`

---

### 6.2. Cập nhật thông tin cá nhân

**📌 Mục đích:** Chỉnh sửa fullName, phone, address

**🔗 Files liên quan:**
- **Controller:** `UserProfileController.java`
  - Method: `updateProfile()` - POST `/profile/update`
- **Service:** `UserProfileService.java`
  - Method: `updateProfile(UserProfileDTO dto)`
- **DTO:** `UserProfileDTO.java`

**🎯 Request Parameters:**
```java
@RequestParam String fullName
@RequestParam String phone
@RequestParam String address
```

**⚙️ Luồng xử lý:**
1. User sửa thông tin → POST `/profile/update`
2. `userProfileService.updateProfile()` cập nhật DB
3. Redirect về `/profile` với flash message

---

### 6.3. Upload avatar

**📌 Mục đích:** Thay đổi ảnh đại diện

**🔗 Files liên quan:**
- **Controller:** `UserProfileController.java`
  - Method: `uploadAvatar()` - POST `/profile/upload-avatar`
- **Service:** `FileUploadService.java`
  - Method: `uploadAvatar(MultipartFile file)`
- **Service:** `UserProfileService.java`
  - Method: `updateAvatar(String avatarUrl)`

**🎯 Request Parameters:**
```java
@RequestParam("file") MultipartFile file  // File upload
```

**🎯 Response (JSON):**
```json
{
  "success": true,
  "avatarUrl": "/uploads/avatars/user_123.jpg"
}
```

**⚙️ Luồng xử lý:**
1. User chọn file → AJAX POST `/profile/upload-avatar`
2. Validate file type (jpg, png)
3. `fileUploadService.uploadAvatar()` save file
4. `userProfileService.updateAvatar()` cập nhật DB
5. Return JSON với URL mới

---

### 6.4. Đổi mật khẩu

**📌 Mục đích:** Thay đổi password

**🔗 Files liên quan:**
- **Controller:** `UserProfileController.java`
  - Method: `changePassword()` - POST `/profile/change-password`
- **Service:** `UserProfileService.java`
  - Method: `changePassword(ChangePasswordDTO dto)`
- **DTO:** `ChangePasswordDTO.java`

**🎯 Request Parameters:**
```java
@RequestParam String currentPassword  // Mật khẩu hiện tại
@RequestParam String newPassword      // Mật khẩu mới
@RequestParam String confirmPassword  // Xác nhận mật khẩu
```

**🎯 Response (JSON):**
```json
{
  "success": true,
  "message": "Đổi mật khẩu thành công"
}
```

**⚙️ Luồng xử lý:**
1. User nhập form → AJAX POST `/profile/change-password`
2. `userProfileService.changePassword()`:
   - Kiểm tra currentPassword đúng
   - Validate newPassword == confirmPassword
   - Encode password mới với BCrypt
   - Cập nhật DB
3. Return JSON response

---

## 7. PAYMENT METHODS - PHƯƠNG THỨC THANH TOÁN

### 7.1. COD (Cash on Delivery)

**📌 Mục đích:** Thanh toán khi nhận hàng

**🔗 Files liên quan:**
- **Controller:** `CheckoutController.java`
  - Method: `placeOrder()` với `paymentMethod = "COD"`
- **Service:** `CheckoutService.java`

**⚙️ Luồng xử lý:**
1. User chọn COD → Submit form
2. Tạo Order với paymentMethod = COD
3. Status = PENDING
4. Admin xử lý và giao hàng
5. Khi giao thành công → Status = DELIVERED

---

### 7.2. VNPay (Online Payment)

**📌 Mục đích:** Thanh toán qua cổng VNPay

**🔗 Files liên quan:**
- **Controller:** `PaymentController.java`
  - Method: `createPayment()` - GET `/payment/create`
  - Method: `paymentCallback()` - GET `/payment/vnpay-return`
- **Service:** `VNPayService.java`
  - Method: `createPaymentUrl(Order order)`
  - Method: `verifyPayment(Map params)`
- **Template:** `templates/payment-result.html`

**🎯 Model Attributes:**
```java
model.addAttribute("orderId", Integer)
model.addAttribute("amount", BigDecimal)
model.addAttribute("resultCode", String)  // 00 = success
model.addAttribute("message", String)
```

**⚙️ Luồng xử lý:**
1. User chọn VNPay → Redirect `/payment/create?orderId=5`
2. `vnpayService.createPaymentUrl()` tạo URL VNPay
3. Redirect user sang VNPay gateway
4. User thanh toán → VNPay callback `/payment/vnpay-return`
5. `vnpayService.verifyPayment()` kiểm tra chữ ký
6. Update order status → CONFIRMED
7. Render `payment-result.html`

---

### 7.3. Banking Transfer (Chuyển khoản)

**📌 Mục đích:** Thanh toán qua chuyển khoản ngân hàng

**🔗 Files liên quan:**
- **Controller:** `CheckoutController.java`
  - Method: `placeOrder()` với `paymentMethod = "BANK_TRANSFER"`
- **Service:** `BankAccountService.java`
  - Method: `getActiveBankAccounts()`
  - Method: `findById(Integer id)`
- **Entity:** `BankAccount.java` (chứa: bankName, accountNumber, qrCodeUrl)

**🎯 Model Attributes:**
```java
model.addAttribute("bankAccounts", List<BankAccount>)  // Danh sách ngân hàng
```

**⚙️ Luồng xử lý:**
1. User chọn Banking → Hiển thị danh sách ngân hàng
2. Chọn ngân hàng → Hiển thị QR code + STK
3. User chuyển khoản thủ công
4. Nhập mã giao dịch → Submit form
5. Tạo Order với bankAccountId
6. Payment status = PENDING (chờ admin xác nhận)

---

## 8. INVOICE - HÓA ĐƠN

### 8.1. Tải hóa đơn Word

**📌 Mục đích:** Download invoice dạng .docx

**🔗 Files liên quan:**
- **Controller:** `InvoiceController.java`
  - Method: `downloadWordInvoice()` - GET `/invoice/{orderId}/word`
- **Service:** `InvoiceService.java`
  - Method: `generateWordInvoice(Integer orderId)`

**🎯 Response:**
```java
Content-Type: application/vnd.openxmlformats-officedocument.wordprocessingml.document
Content-Disposition: attachment; filename="invoice_ORD000005.docx"
```

**⚙️ Luồng xử lý:**
1. User click "Tải Word" → GET `/invoice/5/word`
2. `invoiceService.generateWordInvoice(5)` tạo file .docx
3. Return file as download

---

### 8.2. Tải hóa đơn PDF

**📌 Mục đích:** Download invoice dạng .pdf

**🔗 Files liên quan:**
- **Controller:** `InvoiceController.java`
  - Method: `downloadPdfInvoice()` - GET `/invoice/{orderId}/pdf`
- **Service:** `InvoiceService.java`
  - Method: `generatePdfInvoice(Integer orderId)`

**🎯 Response:**
```java
Content-Type: application/pdf
Content-Disposition: attachment; filename="invoice_ORD000005.pdf"
```

**⚙️ Luồng xử lý:**
1. User click "Tải PDF" → GET `/invoice/5/pdf`
2. `invoiceService.generatePdfInvoice(5)` tạo file PDF
3. Return file as download

---

## 📊 TỔNG KẾT CÁC CONTROLLER & SERVICE USER SỬ DỤNG

### Controllers:

| Controller | Base Path | Main Functions |
|------------|-----------|----------------|
| `PublicController` | `/` | Homepage, Products, Authentication (form login) |
| `UserController` | `/user` | Cart, Orders, Profile (DEPRECATED - moved to separate controllers) |
| `CartController` | `/cart` | Cart management (add, update, remove) |
| `CheckoutController` | `/checkout` | Checkout, Place Order, Phone verification gate |
| `PaymentController` | `/payment` | VNPay payment processing |
| `UserProfileController` | `/profile` | User profile, Change password, Upload avatar, Update phone |
| `InvoiceController` | `/invoice` | Download invoices |
| `PasswordResetController` | `/forgot-password`, `/reset-password` | Password recovery (LOCAL only) |
| `AccountController` | `/account` | Account settings (alternative to /profile) |
| `OrderTrackingController` | `/my-orders` | Order tracking, order history |

### Services:

| Service | Main Functions |
|---------|----------------|
| `UserService` | User registration, email verification (LOCAL) |
| `CustomOAuth2UserService` | **OAuth2 authentication**, Google user creation/update |
| `PhoneVerificationService` | **Phone validation for OAuth2 users**, enforce phone before checkout |
| `UserProfileService` | Profile update, change password, avatar upload |
| `CartService` | Cart CRUD operations |
| `CheckoutService` | Order placement, payment processing |
| `OrderService` | Order management, order tracking |
| `WatchService` | Product listing, search, filter |
| `MailService` | Email notifications |
| `PasswordResetService` | Password recovery (LOCAL only) |

### OAuth2-Specific Files:

| File | Purpose |
|------|---------|
| `CustomOAuth2UserService.java` | Handle OAuth2 login, create/update user from Google |
| `CustomOAuth2User.java` | Wrapper for OAuth2User with authorities |
| `PhoneVerificationService.java` | Validate & verify phone for OAuth2 users |
| `SecurityConfig.java` | Configure oauth2Login, form login, CSRF |
| `CustomLoginSuccessHandler.java` | Handle successful login (both form & OAuth2) |
| `templates/public/login.html` | Login form + Google OAuth2 button |
| `templates/user/profile.html` | Phone verification UI for OAuth2 users |

---

## 🔐 SECURITY & AUTHORIZATION

**Spring Security Configuration:**
- Public paths (no login): `/`, `/login`, `/register`, `/watches/**`, `/products/**`
- Authenticated: `/cart/**`, `/checkout/**`, `/user/**`, `/profile/**`, `/orders/**`
- Role required: `ROLE_USER` for user-specific pages

**Authentication Methods:**
1. **Form Login (LOCAL):**
   - Email/Password authentication
   - POST `/perform-login`
   - UserDetailsService loads user from database
   - Password verified with BCrypt
   - Session created on success

2. **OAuth2 Login (GOOGLE):**
   - Google OAuth2 authorization code flow
   - Entry point: `/oauth2/authorization/google`
   - Callback: `/login/oauth2/code/google`
   - CustomOAuth2UserService handles user creation/update
   - No password required (password = NULL in DB)
   - **Phone verification required before checkout**

**Session Management:**
- Login → Create session (both LOCAL and OAuth2)
- Session timeout: 30 minutes (default)
- Remember me: Optional (checkbox - LOCAL only)
- OAuth2 users: Session persisted normally, no refresh token stored

**Authorization:**
- Both LOCAL and OAuth2 users have same authorities
- Role: `ROLE_USER` (assigned during registration/OAuth2 login)
- Principal type:
  - LOCAL: `UserDetails` (Spring Security default)
  - OAuth2: `OAuth2User` → wrapped as `CustomOAuth2User`
- Controllers use `Principal principal` to support both types

**Login Methods Comparison:**

| Feature | LOCAL (Form Login) | GOOGLE (OAuth2) |
|---------|-------------------|-----------------|
| **Entry Point** | `/login` (form) | `/oauth2/authorization/google` (button) |
| **Authentication** | Email + Password | Google account |
| **Password Required** | ✅ Yes (BCrypt) | ❌ No (password = NULL) |
| **Email Verification** | ✅ Required (email link) | ❌ Not needed (Google verified) |
| **Phone Verification** | ❌ Optional | ✅ **Required before checkout** |
| **Avatar** | Upload manually | Auto from Google picture |
| **Session** | Created on login | Created on OAuth2 callback |
| **Can Change Password** | ✅ Yes | ❌ No (OAuth2 only) |
| **Can Reset Password** | ✅ Yes (email link) | ❌ N/A |
| **Database Fields** | provider = "LOCAL" | provider = "GOOGLE" |
| **User Creation** | Manual registration | Auto on first OAuth2 login |

**CSRF Protection:**
- Enabled for state-changing operations (POST, PUT, DELETE)
- Disabled for: `/cart/add`, `/cart/update`, `/checkout/place-order` (in CSRF ignore list)

---

## 📧 EMAIL NOTIFICATIONS

User nhận email trong các trường hợp:
1. **Đăng ký (LOCAL)** → Email xác thực tài khoản
2. **OAuth2 Login lần đầu** → Email chào mừng (optional, chưa implement)
3. **Đặt hàng** → Email xác nhận đơn hàng
4. **Quên mật khẩu (LOCAL)** → Email reset password
5. **Thay đổi status đơn hàng** → Email thông báo (Shipping, Delivered, etc.)
6. **Hủy đơn** → Email xác nhận hủy

**Note:** OAuth2 users không cần email verification vì Google đã verify email.

---

## 🎨 UI TEMPLATES & MODEL ATTRIBUTES

### Naming Convention:
- Templates: `templates/public/*.html`, `templates/user/*.html`
- Model attributes: camelCase (e.g., `cartItems`, `featuredWatches`)
- Flash messages: `success`, `error`, `warning`, `info`

### Common Attributes:
```java
model.addAttribute("pageTitle", String)      // Title trang
model.addAttribute("currentPage", String)    // Để highlight menu
model.addAttribute("user", User)             // User hiện tại (if authenticated)
model.addAttribute("cartItemCount", int)     // Badge trong header
```

---

## 🔄 DATA FLOW SUMMARY

```
1. USER ACTION (Click/Submit)
   ↓
2. CONTROLLER (Nhận request, validate)
   ↓
3. SERVICE (Business logic, tính toán)
   ↓
4. REPOSITORY (Query database)
   ↓
5. ENTITY (Domain objects)
   ↓
6. SERVICE (Return processed data)
   ↓
7. CONTROLLER (Add to Model, redirect/render)
   ↓
8. VIEW TEMPLATE (Thymeleaf render HTML với data)
   ↓
9. RESPONSE (HTML page hoặc JSON)
```

---

**📝 Ghi chú:** 
- Tài liệu này mô tả tất cả chức năng USER có thể sử dụng trong hệ thống 2BShop
- Hỗ trợ 2 phương thức đăng nhập: **Form Login (LOCAL)** và **OAuth2 Login (GOOGLE)**
- OAuth2 users có đầy đủ tính năng như LOCAL users, nhưng bắt buộc phải verify phone trước khi checkout
- Để xem chức năng ADMIN, tham khảo `ADMIN_FEATURES.md`
- Để xem chi tiết OAuth2 setup, tham khảo `OAUTH2_SETUP_GUIDE.md`
- Để xem chi tiết Phone Verification workflow, tham khảo `PHONE_VERIFICATION_GUIDE.md`
