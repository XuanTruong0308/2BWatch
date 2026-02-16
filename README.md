# 2BShop - E-Commerce Watch Store

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## � Quick Start

### Run Server
```bash
# Option 1: Use start menu
start.bat  # Choose [1] Server only

# Option 2: Manual
cd 2BShop
mvnw.cmd spring-boot:run
```

Access: http://localhost:8080

### VNPay Payment Testing
```bash
# Start server + tunnel
start.bat  # Choose [2] Server + Cloudflared

# Then follow: VNPAY_GUIDE.md
```

---

## �📋 Mô Tả Project

2BShop là một ứng dụng thương mại điện tử chuyên bán đồng hồ, được xây dựng bằng Spring Boot 4.x và Java 21. Hệ thống cung cấp đầy đủ chức năng cho cả khách hàng và quản trị viên.

## ✨ Tính Năng Chính

### 🛍️ Khách Hàng
- **Tài khoản**: Đăng ký, đăng nhập (email/password + OAuth2 Google/Facebook)
- **Sản phẩm**: Xem danh sách, chi tiết, tìm kiếm, lọc theo brand/category/price
- **Giỏ hàng**: Thêm/sửa/xóa sản phẩm, cập nhật số lượng
- **Đặt hàng**: Checkout, chọn địa chỉ giao hàng, áp dụng mã giảm giá
- **Thanh toán**: COD, Bank Transfer, VNPay
- **Email**: Xác thực tài khoản, thông báo đơn hàng

### 👨‍💼 Quản Trị Viên
- **Dashboard**: Thống kê doanh thu, đơn hàng, biểu đồ Chart.js
- **Quản lý sản phẩm**: CRUD watches, upload ảnh, quản lý stock
- **Quản lý đơn hàng**: Xem, cập nhật trạng thái, hủy đơn
- **Quản lý user**: Xem danh sách, ban/unban, thống kê
- **Thanh toán**: Quản lý phương thức, xem giao dịch
- **Ban logs**: Quản lý vi phạm, lịch sử ban user

## 🛠️ Công Nghệ Sử Dụng

### Backend
- **Framework**: Spring Boot 4.0.1
- **Java**: 21 (LTS)
- **Security**: Spring Security 6
- **Database**: SQL Server (JPA/Hibernate)
- **Template Engine**: Thymeleaf
- **Validation**: Spring Validation
- **Email**: Spring Mail (Gmail SMTP)
- **Testing**: JUnit 5, Mockito, MockMvc

### Frontend
- **CSS Framework**: Bootstrap 5
- **Icons**: Font Awesome 6
- **Charts**: Chart.js
- **JavaScript**: Vanilla JS

### Payment Gateway
- **VNPay**: Cổng thanh toán trực tuyến
- **COD**: Thanh toán khi nhận hàng

### OAuth2 Providers
- Google Login
- Facebook Login

## 📁 Cấu Trúc Project

```
2BShop/
├── src/
│   ├── main/
│   │   ├── java/boiz/shop/_2BShop/
│   │   │   ├── config/          # Configuration (Security, WebMVC, OAuth2)
│   │   │   ├── controller/      # Controllers (Admin, Public, User)
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # JPA Entities
│   │   │   ├── respository/     # JPA Repositories
│   │   │   └── service/         # Business Logic
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── db/schema.sql
│   │       ├── static/          # CSS, JS, Images
│   │       └── templates/       # Thymeleaf templates
│   │           ├── admin/       # Admin pages
│   │           ├── public/      # Public pages
│   │           ├── user/        # User pages
│   │           └── fragments/   # Reusable fragments
│   └── test/                    # Unit & Integration Tests
├── target/                      # Build output
└── pom.xml                      # Maven dependencies
```

## 🚀 Cài Đặt và Chạy

### Yêu Cầu
- Java 21
- Maven 3.8+
- SQL Server 2019+
- Node.js (optional - for frontend development)

### Bước 1: Clone Repository
```bash
git clone https://github.com/yourusername/2BShop.git
cd 2BShop
```

### Bước 2: Cấu Hình Database
1. Tạo database `BShopDB` trong SQL Server
2. Chạy script `src/main/resources/db/schema.sql`
3. Cập nhật `application.properties`:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=BShopDB;encrypt=true;trustServerCertificate=true
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Bước 3: Cấu Hình Email (Gmail)
```properties
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
```

### Bước 4: Cấu Hình VNPay
```properties
vnpay.tmnCode=YOUR_TMN_CODE
vnpay.hashSecret=YOUR_HASH_SECRET
vnpay.payUrl=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
```

### Bước 5: Cấu Hình OAuth2
```properties
# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET

# Facebook OAuth2
spring.security.oauth2.client.registration.facebook.client-id=YOUR_FACEBOOK_APP_ID
spring.security.oauth2.client.registration.facebook.client-secret=YOUR_FACEBOOK_APP_SECRET
```

### Bước 6: Build & Run
```bash
cd 2BShop
mvn clean install
mvn spring-boot:run
```

Ứng dụng sẽ chạy tại: `http://localhost:8080`

## 🧪 Testing

### Chạy tất cả tests
```bash
mvn test
```

### Chạy specific test
```bash
mvn test -Dtest=DashboardControllerTest
mvn test -Dtest=DashboardServiceTest
```

### Test Coverage
```bash
mvn clean test jacoco:report
```

## 📸 Screenshots

### Customer Pages
- Homepage: Hiển thị sản phẩm nổi bật
- Product Listing: Lọc theo brand, category, giá
- Product Detail: Chi tiết sản phẩm, thêm giỏ hàng
- Cart: Quản lý giỏ hàng
- Checkout: Đặt hàng, chọn thanh toán

### Admin Dashboard
- Dashboard: Biểu đồ doanh thu, thống kê
- Product Management: CRUD sản phẩm
- Order Management: Quản lý đơn hàng
- User Management: Quản lý user, ban/unban

## 📚 API Endpoints

### Public Endpoints
- `GET /` - Homepage
- `GET /products` - Product listing
- `GET /products/{id}` - Product detail
- `GET /login` - Login page
- `GET /register` - Register page

### User Endpoints (Authenticated)
- `GET /cart` - Shopping cart
- `POST /cart/add` - Add to cart
- `GET /checkout` - Checkout page
- `POST /orders/place` - Place order
- `GET /account` - User account

### Admin Endpoints (Role: ADMIN)
- `GET /admin/dashboard` - Admin dashboard
- `GET /admin/watches` - Manage watches
- `GET /admin/orders` - Manage orders
- `GET /admin/users` - Manage users
- `GET /admin/payment-methods` - Payment methods
- `GET /admin/bans` - Ban management

## 🔐 Security

- **Authentication**: Spring Security với BCrypt password encoding
- **Authorization**: Role-based (USER, ADMIN)
- **OAuth2**: Google, Facebook login
- **CSRF**: Enabled cho forms
- **Session**: Server-side session management

## 📧 Email Templates

- **Account Verification**: Xác thực email khi đăng ký
- **Order Confirmation**: Thông báo đặt hàng thành công
- **Order Status Update**: Thông báo thay đổi trạng thái

## 💳 Payment Methods

1. **COD**: Thanh toán khi nhận hàng
2. **Bank Transfer**: Chuyển khoản ngân hàng
3. **VNPay**: Thanh toán online qua VNPay

## 📝 Database Schema

### Main Tables
- `Users` - Thông tin người dùng
- `Roles` - Vai trò (USER, ADMIN)
- `UserRoles` - Ánh xạ user-role
- `Watches` - Sản phẩm đồng hồ
- `Brands` - Thương hiệu
- `Categories` - Danh mục
- `Orders` - Đơn hàng
- `OrderDetails` - Chi tiết đơn hàng
- `Cart` - Giỏ hàng
- `CartItems` - Item trong giỏ
- `PaymentMethods` - Phương thức thanh toán
- `PaymentTransactions` - Giao dịch
- `BanLogs` - Lịch sử ban user
- `ViolationTypes` - Loại vi phạm

## 🤝 Contributing

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **Boiz Team** - *Initial work*

## 🙏 Acknowledgments

- Spring Boot Documentation
- Bootstrap Team
- VNPay Integration Guide
- OAuth2 Providers (Google, Facebook)

## 📞 Contact

- Email: support@2bshop.com
- Website: https://2bshop.com

---

**Note**: Đây là project học tập. Không sử dụng cho mục đích thương mại mà không có sự cho phép.
