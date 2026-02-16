-- Migration script: Add discount to existing products
-- Run this in SQL Server Management Studio after connecting to BShopDB

USE BShopDB;
GO

-- Update discount_percent for existing watches
UPDATE watches 
SET discount_percent = 5 
WHERE watch_name LIKE '%Omega%' AND (discount_percent IS NULL OR discount_percent = 0);

UPDATE watches 
SET discount_percent = 10 
WHERE watch_name LIKE '%Seiko Presage%' AND (discount_percent IS NULL OR discount_percent = 0);

UPDATE watches 
SET discount_percent = 15 
WHERE watch_name LIKE '%G-Shock%' AND (discount_percent IS NULL OR discount_percent = 0);

UPDATE watches 
SET discount_percent = 8 
WHERE watch_name LIKE '%Tissot PRX%' AND (discount_percent IS NULL OR discount_percent = 0);

UPDATE watches 
SET discount_percent = 20 
WHERE watch_name LIKE '%Fossil%' AND (discount_percent IS NULL OR discount_percent = 0);

UPDATE watches 
SET discount_percent = 12 
WHERE watch_name LIKE '%Seiko 5%' AND (discount_percent IS NULL OR discount_percent = 0);

UPDATE watches 
SET discount_percent = 10 
WHERE watch_name LIKE '%Casio Edifice%' AND (discount_percent IS NULL OR discount_percent = 0);

-- Verify changes
SELECT watch_id, watch_name, price, discount_percent, is_active, created_date
FROM watches
ORDER BY discount_percent DESC, created_date DESC;

PRINT 'Discount update completed!';
