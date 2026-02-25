-- ===================================
-- FIX: Allow NULL password for OAuth2 users
-- ===================================
-- This fixes the error:
-- "Cannot insert the value NULL into column 'password'"
--
-- OAuth2 users (Google login) don't have passwords,
-- so the password column must allow NULL values.
-- ===================================

USE BShopDB;
GO

-- Alter the password column to allow NULL
ALTER TABLE users 
ALTER COLUMN password NVARCHAR(255) NULL;
GO

-- Verify the change
SELECT 
    COLUMN_NAME,
    DATA_TYPE,
    CHARACTER_MAXIMUM_LENGTH,
    IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'users' 
  AND COLUMN_NAME = 'password';
GO

PRINT 'Password column now allows NULL for OAuth2 users';
