-- ================================================
-- MIGRATION: Add Product Specifications Fields
-- Ngày: 2026-01-31
-- Mục đích: Thêm các trường spec chi tiết cho đồng hồ
-- ================================================

USE BShopDB;
GO

-- Thêm các cột mới vào bảng watches
ALTER TABLE watches ADD model_number NVARCHAR(100);
ALTER TABLE watches ADD condition_status NVARCHAR(MAX); -- Tình trạng (dài)
ALTER TABLE watches ADD case_material NVARCHAR(100); -- Vỏ
ALTER TABLE watches ADD case_bezel NVARCHAR(MAX); -- Niềng (có thể dài)
ALTER TABLE watches ADD case_bracelet NVARCHAR(MAX); -- Dây (có thể dài)
ALTER TABLE watches ADD movement NVARCHAR(100); -- Cơ chế máy (Automatic, Quartz, Manual)
ALTER TABLE watches ADD bracelet NVARCHAR (MAX); -- Bracelet (có thể dài)
ALTER TABLE watches ADD glass_type NVARCHAR(50); -- Mặt kính (Shapphire, Mineral, Acrylic)
ALTER TABLE watches ADD dial_description NVARCHAR(MAX); -- Mặt đồng hồ (có thể dài)
ALTER TABLE watches ADD case_diameter NVARCHAR(50); -- Kích thước mặt (e.g. "26 mm")
ALTER TABLE watches ADD water_resistance NVARCHAR(50); -- Độ chịu nước (e.g. "3ATM", "10ATM")
ALTER TABLE watches ADD warranty_period NVARCHAR(100); -- Bảo hành (e.g. "Bảo hành ký thuật 01 năm")
ALTER TABLE watches ADD show_contact_price BIT DEFAULT 0; -- TRUE = Hiển thị "Liên hệ" thay vì giá
GO

-- Update existing Rolex product với thông tin từ ảnh mẫu
UPDATE watches 
SET model_number = 'Model 69178',
    condition_status = N'Đồng hồ còn rất cứng cáp đẹp xuất sắc. Chỉ còn đồng hồ',
    case_material = N'Vàng đúc 18k',
    case_bezel = N'Niềng: Vàng đúc 18k lên kim cương và ruby đỏ 3-6-9-12, Dây: Vàng đúc 18k',
    movement = 'Automatic',
    bracelet = N'Vàng đúc 18k, khóa bấm vàng đúc 18k',
    glass_type = 'Shapphire',
    dial_description = N'Mặt trắng cọc số ruby đỏ, lịch ngày',
    case_diameter = '26 mm',
    water_resistance = '3ATM',
    warranty_period = N'Bảo hành ký thuật 01 năm',
    show_contact_price = 1 -- Hiển thị "Liên hệ" vì giá cao
WHERE watch_id = 1;
GO

-- Verify changes
SELECT watch_id, watch_name, model_number, case_diameter, show_contact_price
FROM watches 
WHERE watch_id = 1;
GO

PRINT 'Migration completed successfully!';
