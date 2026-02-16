    -- ===================================
    -- SHOPPING CART & CHECKOUT MIGRATION
    -- Phase 1: Database Schema
    -- ===================================

    -- ===================================
    -- 1. CREATE COUPONS TABLE
    -- ===================================

    IF OBJECT_ID('coupons', 'U') IS NOT NULL DROP TABLE coupons;

    CREATE TABLE coupons (
        coupon_id INT PRIMARY KEY IDENTITY(1,1),
        code NVARCHAR(50) UNIQUE NOT NULL,
        discount_type NVARCHAR(20) NOT NULL, -- 'PERCENTAGE' or 'FIXED'
        discount_value DECIMAL(18,2) NOT NULL,
        min_order_value DECIMAL(18,2),
        max_discount DECIMAL(18,2),
        usage_limit INT,
        used_count INT DEFAULT 0,
        valid_from DATETIME,
        valid_until DATETIME,
        is_active BIT DEFAULT 1,
        description NVARCHAR(500),
        created_at DATETIME DEFAULT GETDATE(),
        updated_at DATETIME DEFAULT GETDATE()
    );

    -- ===================================
    -- 2. CREATE BANK ACCOUNTS TABLE
    -- ===================================

    IF OBJECT_ID('bank_accounts', 'U') IS NOT NULL DROP TABLE bank_accounts;

    CREATE TABLE bank_accounts (
        bank_account_id INT PRIMARY KEY IDENTITY(1,1),
        bank_name NVARCHAR(100) NOT NULL,
        bank_code NVARCHAR(20) NOT NULL, -- MB, VCB, etc.
        account_number NVARCHAR(50) NOT NULL,
        account_holder NVARCHAR(200) NOT NULL,
        qr_image_url NVARCHAR(500), -- Path to uploaded QR image
        is_active BIT DEFAULT 1,
        display_order INT DEFAULT 0,
        created_at DATETIME DEFAULT GETDATE(),
        updated_at DATETIME DEFAULT GETDATE()
    );

    -- ===================================
    -- 3. ALTER ORDERS TABLE
    -- Add payment and checkout fields
    -- ===================================

    -- Check and add columns if they don't exist
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('orders') AND name = 'payment_method')
        ALTER TABLE orders ADD payment_method NVARCHAR(20); -- 'COD' or 'BANKING'

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('orders') AND name = 'deposit_required')
        ALTER TABLE orders ADD deposit_required BIT DEFAULT 0;

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('orders') AND name = 'deposit_amount')
        ALTER TABLE orders ADD deposit_amount DECIMAL(18,2) DEFAULT 0;

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('orders') AND name = 'deposit_paid')
        ALTER TABLE orders ADD deposit_paid BIT DEFAULT 0;

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('orders') AND name = 'coupon_code')
        ALTER TABLE orders ADD coupon_code NVARCHAR(50);

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('orders') AND name = 'discount_amount')
        ALTER TABLE orders ADD discount_amount DECIMAL(18,2) DEFAULT 0;

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('orders') AND name = 'shipping_notes')
        ALTER TABLE orders ADD shipping_notes NVARCHAR(1000);

    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('orders') AND name = 'bank_account_id')
        ALTER TABLE orders ADD bank_account_id INT;

    -- Add foreign key for bank_account_id
    IF NOT EXISTS (SELECT * FROM sys.foreign_keys WHERE name = 'FK_orders_bank_accounts')
        ALTER TABLE orders ADD CONSTRAINT FK_orders_bank_accounts 
        FOREIGN KEY (bank_account_id) REFERENCES bank_accounts(bank_account_id);

    -- ===================================
    -- 4. INSERT INITIAL DATA
    -- ===================================

    -- Insert initial bank accounts (MBBank, Vietcombank)
    INSERT INTO bank_accounts (bank_name, bank_code, account_number, account_holder, is_active, display_order)
    VALUES 
        (N'MB Bank', 'MB', '0123456789', N'CÔNG TY TNHH 2BSHOP', 1, 1),
        (N'Vietcombank', 'VCB', '9876543210', N'CÔNG TY TNHH 2BSHOP', 1, 2);

    -- Insert sample coupons for testing
    INSERT INTO coupons (code, discount_type, discount_value, min_order_value, max_discount, usage_limit, valid_from, valid_until, is_active, description)
    VALUES 
        ('WELCOME10', 'PERCENTAGE', 10, 1000000, 5000000, 100, GETDATE(), DATEADD(MONTH, 3, GETDATE()), 1, N'Giảm 10% cho đơn hàng đầu tiên'),
        ('SALE50K', 'FIXED', 50000, 500000, NULL, 50, GETDATE(), DATEADD(MONTH, 1, GETDATE()), 1, N'Giảm 50,000đ cho đơn từ 500,000đ'),
        ('VIP20', 'PERCENTAGE', 20, 10000000, 10000000, 20, GETDATE(), DATEADD(MONTH, 6, GETDATE()), 1, N'Giảm 20% cho đơn từ 10 triệu');

    -- ===================================
    -- 5. INSERT PAYMENT METHODS
    -- ===================================
    
    IF OBJECT_ID('payment_methods', 'U') IS NOT NULL
    BEGIN
        IF NOT EXISTS (SELECT * FROM payment_methods WHERE method_name = 'COD')
            INSERT INTO payment_methods (method_name, description, is_active, created_date, updated_date)
            VALUES ('COD', N'Thanh toán khi nhận hàng', 1, GETDATE(), GETDATE());

        IF NOT EXISTS (SELECT * FROM payment_methods WHERE method_name = 'BANKING')
            INSERT INTO payment_methods (method_name, description, is_active, created_date, updated_date)
            VALUES ('BANKING', N'Chuyển khoản ngân hàng', 1, GETDATE(), GETDATE());
    END

    PRINT 'Migration completed successfully!';
    PRINT 'Created tables: coupons, bank_accounts';
    PRINT 'Updated table: orders (added payment fields)';
    PRINT 'Inserted 2 bank accounts, 3 sample coupons, and payment methods';
