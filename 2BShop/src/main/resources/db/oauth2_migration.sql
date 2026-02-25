-- ===================================
-- OAUTH2 MIGRATION SCRIPT
-- Cập nhật database để hỗ trợ Google & Facebook Login
-- ===================================

USE BShopDB;
GO

PRINT 'Starting OAuth2 Migration...';
GO

-- ===================================
-- 1. ADD NEW COLUMNS
-- ===================================

-- Thêm cột provider (LOCAL, GOOGLE, FACEBOOK)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'provider')
BEGIN
    PRINT 'Adding column: provider';
    ALTER TABLE users ADD provider NVARCHAR(20) DEFAULT 'LOCAL';
END
ELSE
BEGIN
    PRINT 'Column provider already exists';
END
GO

-- Thêm cột provider_id (ID từ Google/Facebook)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'provider_id')
BEGIN
    PRINT 'Adding column: provider_id';
    ALTER TABLE users ADD provider_id NVARCHAR(100);
END
ELSE
BEGIN
    PRINT 'Column provider_id already exists';
END
GO

-- Thêm cột email_verified (OAuth2 users auto-verified)
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('users') AND name = 'email_verified')
BEGIN
    PRINT 'Adding column: email_verified';
    ALTER TABLE users ADD email_verified BIT DEFAULT 0;
END
ELSE
BEGIN
    PRINT 'Column email_verified already exists';
END
GO

-- ===================================
-- 2. MODIFY EXISTING COLUMNS
-- ===================================

-- Cho phép password NULL (OAuth2 users không có password)
PRINT 'Modifying column: password (allow NULL)';
ALTER TABLE users ALTER COLUMN password NVARCHAR(255);
GO

-- ===================================
-- 3. UPDATE EXISTING DATA
-- ===================================

-- Update existing users với giá trị mặc định
PRINT 'Updating existing users...';
UPDATE users 
SET 
    provider = 'LOCAL',
    email_verified = is_enabled
WHERE provider IS NULL;
GO

PRINT 'Updated ' + CAST(@@ROWCOUNT AS VARCHAR) + ' existing users';
GO

-- ===================================
-- 4. CREATE INDEXES (Optional - Performance)
-- ===================================

-- Index cho provider (để query nhanh)
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_users_provider' AND object_id = OBJECT_ID('users'))
BEGIN
    PRINT 'Creating index: IX_users_provider';
    CREATE INDEX IX_users_provider ON users(provider);
END
ELSE
BEGIN
    PRINT 'Index IX_users_provider already exists';
END
GO

-- Index cho provider_id (để tìm user OAuth2 nhanh)
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_users_provider_id' AND object_id = OBJECT_ID('users'))
BEGIN
    PRINT 'Creating index: IX_users_provider_id';
    CREATE INDEX IX_users_provider_id ON users(provider_id);
END
ELSE
BEGIN
    PRINT 'Index IX_users_provider_id already exists';
END
GO

-- Composite index cho provider + provider_id (cho OAuth2 lookup)
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_users_provider_provider_id' AND object_id = OBJECT_ID('users'))
BEGIN
    PRINT 'Creating composite index: IX_users_provider_provider_id';
    CREATE INDEX IX_users_provider_provider_id ON users(provider, provider_id);
END
ELSE
BEGIN
    PRINT 'Index IX_users_provider_provider_id already exists';
END
GO

-- ===================================
-- 5. VERIFY MIGRATION
-- ===================================

PRINT '';
PRINT '===================================';
PRINT 'Verification Report';
PRINT '===================================';

-- Check columns
SELECT 
    COLUMN_NAME AS [Column],
    DATA_TYPE AS [Type],
    CHARACTER_MAXIMUM_LENGTH AS [Length],
    IS_NULLABLE AS [Nullable],
    COLUMN_DEFAULT AS [Default]
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'users' 
  AND COLUMN_NAME IN ('provider', 'provider_id', 'email_verified', 'password')
ORDER BY 
    CASE COLUMN_NAME
        WHEN 'provider' THEN 1
        WHEN 'provider_id' THEN 2
        WHEN 'email_verified' THEN 3
        WHEN 'password' THEN 4
    END;
GO

-- Count users by provider
PRINT '';
PRINT 'Users by Provider:';
SELECT 
    provider AS [Provider],
    COUNT(*) AS [Count]
FROM users
GROUP BY provider
ORDER BY COUNT(*) DESC;
GO

-- Sample data
PRINT '';
PRINT 'Sample Users (Top 5):';
SELECT TOP 5
    user_id AS [ID],
    username AS [Username],
    email AS [Email],
    provider AS [Provider],
    provider_id AS [Provider ID],
    email_verified AS [Verified],
    is_enabled AS [Enabled]
FROM users
ORDER BY created_date DESC;
GO

PRINT '';
PRINT '===================================';
PRINT 'OAuth2 Migration Completed!';
PRINT '===================================';
PRINT '';
PRINT 'Next Steps:';
PRINT '1. Update application.properties with Google & Facebook credentials';
PRINT '2. Add OAuth2 login buttons to login.html';
PRINT '3. Test login with Google';
PRINT '4. Test login with Facebook';
PRINT '';
PRINT 'See OAUTH2_SETUP_GUIDE.md for detailed instructions';
GO
