-- Fix NULL values in OAuth2 fields for existing users
-- Run this script to fix NullPointerException errors

USE BShopDB;
GO

-- Update NULL email_verified to 0 for existing users
UPDATE users 
SET email_verified = 0 
WHERE email_verified IS NULL;

-- Update NULL phone_verified to 0 for existing users  
UPDATE users
SET phone_verified = 0
WHERE phone_verified IS NULL;

-- Update NULL provider to 'LOCAL' for existing users
UPDATE users
SET provider = 'LOCAL'
WHERE provider IS NULL;

-- Verify the fix
SELECT 
    user_id, 
    email, 
    provider, 
    email_verified, 
    phone_verified,
    is_enabled
FROM users
ORDER BY created_date DESC;

PRINT 'Fix completed! All NULL values updated to defaults.';
GO
