-- ===================================
-- SCHEMA MIGRATION - BỔ SUNG THIẾU SÓT
-- Cập nhật database để khớp với tất cả Entity
-- ===================================

USE BShopDB;
GO

PRINT '========================================';
PRINT 'STARTING SCHEMA MIGRATION';
PRINT '========================================';
PRINT '';

-- ===================================
-- 1. TẠO TABLE PASSWORD_RESET_TOKENS (THIẾU HOÀN TOÀN)
-- ===================================

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'password_reset_tokens') AND type = 'U')
BEGIN
    PRINT '✓ Creating table: password_reset_tokens';
    
    CREATE TABLE password_reset_tokens (
        token_id INT PRIMARY KEY IDENTITY(1,1),
        token NVARCHAR(255) UNIQUE NOT NULL,
        user_id INT NOT NULL,
        expiry_date DATETIME NOT NULL,
        used BIT DEFAULT 0,
        created_date DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
    );
    
    PRINT '  → Table password_reset_tokens created successfully';
END
ELSE
BEGIN
    PRINT '✓ Table password_reset_tokens already exists';
END
GO

-- ===================================
-- 2. BỔ SUNG COLUMNS CHO CART_ITEMS
-- ===================================

-- Thêm column: price (giá tại thời điểm thêm vào giỏ)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('cart_items') AND name = 'price')
BEGIN
    PRINT '✓ Adding column: cart_items.price';
    ALTER TABLE cart_items ADD price DECIMAL(18,2);
    PRINT '  → Column price added';
END
ELSE
BEGIN
    PRINT '✓ Column cart_items.price already exists';
END
GO

-- Thêm column: is_selected (checkbox để chọn items thanh toán)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('cart_items') AND name = 'is_selected')
BEGIN
    PRINT '✓ Adding column: cart_items.is_selected';
    ALTER TABLE cart_items ADD is_selected BIT DEFAULT 1;
    PRINT '  → Column is_selected added';
    
    -- Update existing records
    UPDATE cart_items SET is_selected = 1 WHERE is_selected IS NULL;
    PRINT '  → Updated existing cart items';
END
ELSE
BEGIN
    PRINT '✓ Column cart_items.is_selected already exists';
END
GO

-- ===================================
-- 3. BỔ SUNG COLUMNS CHO PAYMENT_METHODS
-- ===================================

-- Thêm column: created_date
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('payment_methods') AND name = 'created_date')
BEGIN
    PRINT '✓ Adding column: payment_methods.created_date';
    ALTER TABLE payment_methods ADD created_date DATETIME DEFAULT GETDATE();
    PRINT '  → Column created_date added';
    
    -- Update existing records
    UPDATE payment_methods SET created_date = GETDATE() WHERE created_date IS NULL;
END
ELSE
BEGIN
    PRINT '✓ Column payment_methods.created_date already exists';
END
GO

-- Thêm column: updated_date
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('payment_methods') AND name = 'updated_date')
BEGIN
    PRINT '✓ Adding column: payment_methods.updated_date';
    ALTER TABLE payment_methods ADD updated_date DATETIME DEFAULT GETDATE();
    PRINT '  → Column updated_date added';
    
    -- Update existing records
    UPDATE payment_methods SET updated_date = GETDATE() WHERE updated_date IS NULL;
END
ELSE
BEGIN
    PRINT '✓ Column payment_methods.updated_date already exists';
END
GO

-- ===================================
-- 4. BỔ SUNG PHONE_VERIFIED CHO USERS (OAuth2 requirement)
-- ===================================

-- Thêm column: phone_verified (để kiểm tra user OAuth2 đã cập nhật phone chưa)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'phone_verified')
BEGIN
    PRINT '✓ Adding column: users.phone_verified';
    ALTER TABLE users ADD phone_verified BIT DEFAULT 0;
    PRINT '  → Column phone_verified added';
    
    -- Update existing users:
    -- - LOCAL users có phone → phone_verified = 1
    -- - OAuth2 users hoặc không có phone → phone_verified = 0
    UPDATE users 
    SET phone_verified = CASE 
        WHEN provider = 'LOCAL' AND phone IS NOT NULL AND LEN(phone) >= 10 THEN 1
        ELSE 0
    END
    WHERE phone_verified IS NULL;
    
    PRINT '  → Updated existing users phone_verified status';
END
ELSE
BEGIN
    PRINT '✓ Column users.phone_verified already exists';
END
GO

-- ===================================
-- 5. TẠO INDEXES CHO PERFORMANCE
-- ===================================

-- Index cho password_reset_tokens.token (tìm token nhanh)
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_password_reset_tokens_token' AND object_id = OBJECT_ID('password_reset_tokens'))
BEGIN
    PRINT '✓ Creating index: IX_password_reset_tokens_token';
    CREATE INDEX IX_password_reset_tokens_token ON password_reset_tokens(token);
END
ELSE
BEGIN
    PRINT '✓ Index IX_password_reset_tokens_token already exists';
END
GO

-- Index cho password_reset_tokens.user_id (tìm theo user)
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_password_reset_tokens_user_id' AND object_id = OBJECT_ID('password_reset_tokens'))
BEGIN
    PRINT '✓ Creating index: IX_password_reset_tokens_user_id';
    CREATE INDEX IX_password_reset_tokens_user_id ON password_reset_tokens(user_id);
END
ELSE
BEGIN
    PRINT '✓ Index IX_password_reset_tokens_user_id already exists';
END
GO

-- Index cho cart_items.is_selected (filter selected items)
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_cart_items_is_selected' AND object_id = OBJECT_ID('cart_items'))
BEGIN
    PRINT '✓ Creating index: IX_cart_items_is_selected';
    CREATE INDEX IX_cart_items_is_selected ON cart_items(is_selected);
END
ELSE
BEGIN
    PRINT '✓ Index IX_cart_items_is_selected already exists';
END
GO

-- ===================================
-- 6. VERIFICATION REPORT
-- ===================================

PRINT '';
PRINT '========================================';
PRINT 'VERIFICATION REPORT';
PRINT '========================================';
PRINT '';

-- Check password_reset_tokens table
PRINT '1. PASSWORD_RESET_TOKENS TABLE:';
IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'password_reset_tokens') AND type = 'U')
BEGIN
    SELECT 
        COLUMN_NAME AS [Column],
        DATA_TYPE AS [Type],
        IS_NULLABLE AS [Nullable],
        COLUMN_DEFAULT AS [Default]
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_NAME = 'password_reset_tokens'
    ORDER BY ORDINAL_POSITION;
END
ELSE
BEGIN
    PRINT '   ❌ Table not found!';
END
GO

-- Check cart_items columns
PRINT '';
PRINT '2. CART_ITEMS NEW COLUMNS:';
SELECT 
    COLUMN_NAME AS [Column],
    DATA_TYPE AS [Type],
    IS_NULLABLE AS [Nullable],
    COLUMN_DEFAULT AS [Default]
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'cart_items' 
  AND COLUMN_NAME IN ('price', 'is_selected')
ORDER BY ORDINAL_POSITION;
GO

-- Check payment_methods columns
PRINT '';
PRINT '3. PAYMENT_METHODS NEW COLUMNS:';
SELECT 
    COLUMN_NAME AS [Column],
    DATA_TYPE AS [Type],
    IS_NULLABLE AS [Nullable]
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'payment_methods' 
  AND COLUMN_NAME IN ('created_date', 'updated_date')
ORDER BY ORDINAL_POSITION;
GO

-- Check users OAuth2 columns
PRINT '';
PRINT '4. USERS OAUTH2 + PHONE COLUMNS:';
SELECT 
    COLUMN_NAME AS [Column],
    DATA_TYPE AS [Type],
    IS_NULLABLE AS [Nullable],
    COLUMN_DEFAULT AS [Default]
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'users' 
  AND COLUMN_NAME IN ('provider', 'provider_id', 'email_verified', 'phone_verified')
ORDER BY 
    CASE COLUMN_NAME
        WHEN 'provider' THEN 1
        WHEN 'provider_id' THEN 2
        WHEN 'email_verified' THEN 3
        WHEN 'phone_verified' THEN 4
    END;
GO

-- Count users by provider and phone_verified status
PRINT '';
PRINT '5. USERS PHONE VERIFICATION STATUS:';
SELECT 
    provider AS [Provider],
    phone_verified AS [Phone Verified],
    COUNT(*) AS [Count]
FROM users
GROUP BY provider, phone_verified
ORDER BY provider, phone_verified;
GO

PRINT '';
PRINT '========================================';
PRINT 'MIGRATION COMPLETED SUCCESSFULLY!';
PRINT '========================================';
PRINT '';
PRINT 'Summary of Changes:';
PRINT '✓ Created table: password_reset_tokens';
PRINT '✓ Added cart_items.price (for price history)';
PRINT '✓ Added cart_items.is_selected (for checkout selection)';
PRINT '✓ Added payment_methods.created_date';
PRINT '✓ Added payment_methods.updated_date';
PRINT '✓ Added users.phone_verified (OAuth2 requirement)';
PRINT '✓ Created performance indexes';
PRINT '';
PRINT 'Next Steps:';
PRINT '1. Test password reset functionality';
PRINT '2. Test cart item selection in checkout';
PRINT '3. Implement phone verification for OAuth2 users';
PRINT '4. Add validation: OAuth2 users must verify phone before checkout';
PRINT '';
GO
