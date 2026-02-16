-- ===================================
-- FIX IMAGE PATHS
-- Sửa path ảnh từ /uploads/bshop/watches/ thành /uploads/watches/
-- ===================================

-- Update tất cả image_url trong watch_images
UPDATE watch_images 
SET image_url = REPLACE(image_url, '/uploads/bshop/watches/', '/uploads/watches/')
WHERE image_url LIKE '/uploads/bshop/watches/%';

-- Kiểm tra kết quả
SELECT image_id, watch_id, image_url, is_primary 
FROM watch_images
ORDER BY watch_id, is_primary DESC;
