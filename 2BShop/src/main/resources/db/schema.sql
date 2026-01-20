-- ===================================
-- 2BSHOP DATABASE SCHEMA
-- ===================================

-- Drop tables if exist (for clean setup)
IF OBJECT_ID('ban_logs', 'U') IS NOT NULL DROP TABLE ban_logs;
IF OBJECT_ID('payment_transactions', 'U') IS NOT NULL DROP TABLE payment_transactions;
IF OBJECT_ID('order_details', 'U') IS NOT NULL DROP TABLE order_details;
IF OBJECT_ID('orders', 'U') IS NOT NULL DROP TABLE orders;
IF OBJECT_ID('cart_items', 'U') IS NOT NULL DROP TABLE cart_items;
IF OBJECT_ID('carts', 'U') IS NOT NULL DROP TABLE carts;
IF OBJECT_ID('watch_images', 'U') IS NOT NULL DROP TABLE watch_images;
IF OBJECT_ID('watches', 'U') IS NOT NULL DROP TABLE watches;
IF OBJECT_ID('watch_categories', 'U') IS NOT NULL DROP TABLE watch_categories;
IF OBJECT_ID('watch_brands', 'U') IS NOT NULL DROP TABLE watch_brands;
IF OBJECT_ID('verification_tokens', 'U') IS NOT NULL DROP TABLE verification_tokens;
IF OBJECT_ID('user_roles', 'U') IS NOT NULL DROP TABLE user_roles;
IF OBJECT_ID('roles', 'U') IS NOT NULL DROP TABLE roles;
IF OBJECT_ID('users', 'U') IS NOT NULL DROP TABLE users;
IF OBJECT_ID('violation_types', 'U') IS NOT NULL DROP TABLE violation_types;
IF OBJECT_ID('payment_methods', 'U') IS NOT NULL DROP TABLE payment_methods;

-- ===================================
-- 1. USER MANAGEMENT
-- ===================================

-- Table: users
CREATE TABLE users (
    user_id INT PRIMARY KEY IDENTITY(1,1),
    username NVARCHAR(50) UNIQUE NOT NULL,
    password NVARCHAR(255) NOT NULL,
    email NVARCHAR(100) UNIQUE NOT NULL,
    full_name NVARCHAR(100),
    phone NVARCHAR(20),
    address NVARCHAR(255),
    avatar_url NVARCHAR(255),
    is_enabled BIT DEFAULT 0,
    is_banned BIT DEFAULT 0,
    created_date DATETIME DEFAULT GETDATE(),
    updated_date DATETIME DEFAULT GETDATE()
);

-- Table: roles
CREATE TABLE roles (
    role_id INT PRIMARY KEY IDENTITY(1,1),
    role_name NVARCHAR(20) UNIQUE NOT NULL
);

-- Table: user_roles
CREATE TABLE user_roles (
    user_role_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE
);

-- Table: verification_tokens
CREATE TABLE verification_tokens (
    token_id INT PRIMARY KEY IDENTITY(1,1),
    token NVARCHAR(255) NOT NULL,
    user_id INT NOT NULL,
    expiry_date DATETIME NOT NULL,
    created_date DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ===================================
-- 2. PRODUCT MANAGEMENT
-- ===================================

-- Table: watch_brands
CREATE TABLE watch_brands (
    brand_id INT PRIMARY KEY IDENTITY(1,1),
    brand_name NVARCHAR(50) UNIQUE NOT NULL,
    description NVARCHAR(500),
    logo_url NVARCHAR(255),
    is_active BIT DEFAULT 1
);

-- Table: watch_categories
CREATE TABLE watch_categories (
    category_id INT PRIMARY KEY IDENTITY(1,1),
    category_name NVARCHAR(50) UNIQUE NOT NULL,
    description NVARCHAR(500),
    is_active BIT DEFAULT 1
);

-- Table: watches
CREATE TABLE watches (
    watch_id INT PRIMARY KEY IDENTITY(1,1),
    watch_name NVARCHAR(200) NOT NULL,
    description NVARCHAR(MAX),
    price DECIMAL(18,2) NOT NULL,
    discount_percent INT DEFAULT 0,
    stock_quantity INT DEFAULT 0,
    sold_count INT DEFAULT 0,
    brand_id INT NOT NULL,
    category_id INT NOT NULL,
    is_active BIT DEFAULT 1,
    created_date DATETIME DEFAULT GETDATE(),
    updated_date DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (brand_id) REFERENCES watch_brands(brand_id),
    FOREIGN KEY (category_id) REFERENCES watch_categories(category_id)
);

-- Table: watch_images
CREATE TABLE watch_images (
    image_id INT PRIMARY KEY IDENTITY(1,1),
    watch_id INT NOT NULL,
    image_url NVARCHAR(255) NOT NULL,
    is_primary BIT DEFAULT 0,
    FOREIGN KEY (watch_id) REFERENCES watches(watch_id) ON DELETE CASCADE
);

-- ===================================
-- 3. SHOPPING CART
-- ===================================

-- Table: carts
CREATE TABLE carts (
    cart_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL,
    created_date DATETIME DEFAULT GETDATE(),
    updated_date DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Table: cart_items
CREATE TABLE cart_items (
    cart_item_id INT PRIMARY KEY IDENTITY(1,1),
    cart_id INT NOT NULL,
    watch_id INT NOT NULL,
    quantity INT NOT NULL,
    added_date DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (watch_id) REFERENCES watches(watch_id)
);

-- ===================================
-- 4. PAYMENT METHODS
-- ===================================

-- Table: payment_methods
CREATE TABLE payment_methods (
    payment_method_id INT PRIMARY KEY IDENTITY(1,1),
    method_name NVARCHAR(50) UNIQUE NOT NULL,
    description NVARCHAR(500),
    is_active BIT DEFAULT 1
);

-- ===================================
-- 5. ORDER MANAGEMENT
-- ===================================

-- Table: orders
CREATE TABLE orders (
    order_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL,
    total_amount DECIMAL(18,2) NOT NULL,
    shipping_address NVARCHAR(255) NOT NULL,
    shipping_phone NVARCHAR(20) NOT NULL,
    receiver_name NVARCHAR(100) NOT NULL,
    order_status NVARCHAR(20) DEFAULT 'PENDING',
    payment_method_id INT NOT NULL,
    order_date DATETIME DEFAULT GETDATE(),
    updated_date DATETIME DEFAULT GETDATE(),
    notes NVARCHAR(500),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (payment_method_id) REFERENCES payment_methods(payment_method_id)
);

-- Table: order_details
CREATE TABLE order_details (
    order_detail_id INT PRIMARY KEY IDENTITY(1,1),
    order_id INT NOT NULL,
    watch_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(18,2) NOT NULL,
    discount_amount DECIMAL(18,2) DEFAULT 0,
    subtotal DECIMAL(18,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (watch_id) REFERENCES watches(watch_id)
);

-- Table: payment_transactions
CREATE TABLE payment_transactions (
    transaction_id INT PRIMARY KEY IDENTITY(1,1),
    order_id INT NOT NULL,
    transaction_code NVARCHAR(100),
    payment_method_id INT NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    status NVARCHAR(20) DEFAULT 'PENDING',
    transaction_date DATETIME DEFAULT GETDATE(),
    response_data NVARCHAR(MAX),
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (payment_method_id) REFERENCES payment_methods(payment_method_id)
);

-- ===================================
-- 6. SECURITY & BAN SYSTEM
-- ===================================

-- Table: violation_types
CREATE TABLE violation_types (
    violation_type_id INT PRIMARY KEY IDENTITY(1,1),
    type_name NVARCHAR(50) UNIQUE NOT NULL,
    description NVARCHAR(500)
);

-- Table: ban_logs
CREATE TABLE ban_logs (
    ban_log_id INT PRIMARY KEY IDENTITY(1,1),
    user_id INT NOT NULL,
    violation_type_id INT NOT NULL,
    violation_count INT NOT NULL,
    ban_duration_minutes INT,
    ban_start_date DATETIME DEFAULT GETDATE(),
    ban_end_date DATETIME,
    is_active BIT DEFAULT 1,
    reason NVARCHAR(500),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (violation_type_id) REFERENCES violation_types(violation_type_id)
);

-- ===================================
-- INSERT INITIAL DATA
-- ===================================

-- Insert roles
INSERT INTO roles (role_name) VALUES ('ADMIN'), ('USER');

-- Insert violation types
INSERT INTO violation_types (type_name, description) 
VALUES 
('SQL_INJECTION', N'Phát hiện SQL Injection attempt'),
('XSS', N'Phát hiện Cross-Site Scripting attempt'),
('BRUTE_FORCE', N'Phát hiện brute force login attempt');

-- Insert payment methods
INSERT INTO payment_methods (method_name, description) 
VALUES 
('COD', N'Thanh toán khi nhận hàng (Cash on Delivery)'),
('VNPAY', N'Thanh toán qua VNPay'),
('MOMO', N'Thanh toán qua Ví MoMo'),
('BANK_TRANSFER', N'Chuyển khoản ngân hàng');

-- Insert watch brands
INSERT INTO watch_brands (brand_name, description, is_active) 
VALUES 
(N'Rolex', N'Thương hiệu đồng hồ xa xỉ hàng đầu thế giới', 1),
(N'Omega', N'Đồng hồ Thụy Sỹ cao cấp với lịch sử lâu đời', 1),
(N'Seiko', N'Thương hiệu đồng hồ Nhật Bản nổi tiếng', 1),
(N'Casio', N'Đồng hồ Nhật Bản giá tốt, bền bỉ', 1),
(N'Citizen', N'Đồng hồ Eco-Drive công nghệ năng lượng ánh sáng', 1),
(N'Tissot', N'Đồng hồ Thụy Sỹ giá tầm trung', 1),
(N'Tag Heuer', N'Đồng hồ thể thao cao cấp', 1),
(N'Fossil', N'Đồng hồ thời trang Mỹ', 1);

-- Insert watch categories
INSERT INTO watch_categories (category_name, description, is_active) 
VALUES 
(N'Đồng hồ Nam', N'Đồng hồ dành cho nam giới', 1),
(N'Đồng hồ Nữ', N'Đồng hồ dành cho nữ giới', 1),
(N'Đồng hồ Thể thao', N'Đồng hồ chuyên dụng thể thao', 1),
(N'Đồng hồ Thông minh', N'Smartwatch và wearable devices', 1),
(N'Đồng hồ Cơ', N'Đồng hồ cơ tự động', 1),
(N'Đồng hồ Điện tử', N'Đồng hồ digital', 1);

-- Insert sample watches
INSERT INTO watches (watch_name, description, price, discount_percent, stock_quantity, brand_id, category_id, is_active) 
VALUES 
(N'Rolex Submariner Date', N'Đồng hồ lặn cao cấp với khả năng chống nước 300m', 250000000, 0, 5, 1, 1, 1),
(N'Omega Seamaster', N'Đồng hồ chính thức của James Bond', 180000000, 5, 8, 2, 1, 1),
(N'Seiko Presage Cocktail', N'Đồng hồ cơ tự động phong cách vintage', 12000000, 10, 20, 3, 5, 1),
(N'Casio G-Shock GA-2100', N'Đồng hồ thể thao bền bỉ, chống sốc', 3500000, 15, 50, 4, 3, 1),
(N'Citizen Eco-Drive', N'Đồng hồ năng lượng ánh sáng không cần pin', 8000000, 0, 30, 5, 1, 1),
(N'Tissot PRX Powermatic 80', N'Đồng hồ cơ tự động thanh lịch', 15000000, 8, 15, 6, 1, 1),
(N'Tag Heuer Carrera', N'Đồng hồ đua xe thể thao sang trọng', 95000000, 0, 10, 7, 3, 1),
(N'Fossil Gen 6', N'Smartwatch với Wear OS', 7500000, 20, 40, 8, 4, 1),
(N'Seiko 5 Sports', N'Đồng hồ cơ tự động giá tốt', 5500000, 12, 35, 3, 3, 1),
(N'Casio Edifice', N'Đồng hồ thể thao công nghệ cao', 4200000, 10, 25, 4, 3, 1);

-- Insert sample watch images (primary images for each watch)
INSERT INTO watch_images (watch_id, image_url, is_primary) 
VALUES 
(1, '/images/watches/rolex-submariner.jpg', 1),
(2, '/images/watches/omega-seamaster.jpg', 1),
(3, '/images/watches/seiko-presage.jpg', 1),
(4, '/images/watches/casio-gshock.jpg', 1),
(5, '/images/watches/citizen-ecodrive.jpg', 1),
(6, '/images/watches/tissot-prx.jpg', 1),
(7, '/images/watches/tagheuer-carrera.jpg', 1),
(8, '/images/watches/fossil-gen6.jpg', 1),
(9, '/images/watches/seiko5-sports.jpg', 1),
(10, '/images/watches/casio-edifice.jpg', 1);

-- Update sold_count for demo (to have best sellers)
UPDATE watches SET sold_count = 45 WHERE watch_id = 4; -- G-Shock
UPDATE watches SET sold_count = 38 WHERE watch_id = 8; -- Fossil Gen 6
UPDATE watches SET sold_count = 32 WHERE watch_id = 9; -- Seiko 5 Sports

-- Insert admin user (password: admin123 - remember to encode with BCrypt in production)
-- BCrypt hash for "admin123": $2a$10$XXX... (you need to generate this)
INSERT INTO users (username, password, email, full_name, phone, is_enabled, is_banned) 
VALUES 
('admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'admin@2bshop.com', N'Administrator', '0123456789', 1, 0);

-- Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role_id) 
VALUES (1, 1); -- user_id=1 (admin), role_id=1 (ADMIN)

GO
