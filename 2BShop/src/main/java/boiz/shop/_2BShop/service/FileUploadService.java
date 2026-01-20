package boiz.shop._2BShop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Service xử lý upload và resize ảnh
 */
@Service
public class FileUploadService {
    
    @Value("${app.upload.dir}")
    private String uploadDir;
    
    private static final int TARGET_SIZE = 800; // 800x800px
    
    /**
     * Upload và resize ảnh
     */
    public String uploadImage(MultipartFile file) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IOException("File rỗng!");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("File không phải là ảnh!");
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + extension;
        
        // Create upload directory if not exists
        File uploadDirFile = new File(uploadDir);
        if (!uploadDirFile.exists()) {
            uploadDirFile.mkdirs();
        }
        
        // Read image
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        
        if (originalImage == null) {
            throw new IOException("Không thể đọc file ảnh!");
        }
        
        // Resize to 800x800 (giữ tỷ lệ)
        BufferedImage resizedImage = resizeImage(originalImage, TARGET_SIZE, TARGET_SIZE);
        
        // Save to disk
        File outputFile = new File(uploadDir + File.separator + newFilename);
        String formatName = extension.substring(1).toLowerCase();
        if (formatName.equals("jpg")) {
            formatName = "jpeg";
        }
        ImageIO.write(resizedImage, formatName, outputFile);
        
        // Return filename (không có path, chỉ tên file)
        return newFilename;
    }
    
    /**
     * Resize image giữ tỷ lệ
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        // Tính toán kích thước mới giữ tỷ lệ
        double aspectRatio = (double) originalWidth / originalHeight;
        int newWidth = targetWidth;
        int newHeight = targetHeight;
        
        if (aspectRatio > 1) {
            newHeight = (int) (targetWidth / aspectRatio);
        } else {
            newWidth = (int) (targetHeight * aspectRatio);
        }
        
        // Resize
        BufferedImage resizedImage = new BufferedImage(
            targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        
        // Fill background với màu trắng
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, targetWidth, targetHeight);
        
        // Set rendering hints for better quality
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Vẽ ảnh ở giữa
        int x = (targetWidth - newWidth) / 2;
        int y = (targetHeight - newHeight) / 2;
        g.drawImage(originalImage, x, y, newWidth, newHeight, null);
        g.dispose();
        
        return resizedImage;
    }
    
    /**
     * Upload watch image (tất cả vào chung folder watches/)
     * @param file MultipartFile
     * @param subfolder "main" hoặc "gallery" (chỉ để phân biệt logic, không tạo subfolder)
     * @return Filename only: uuid.jpg (will be accessed via /uploads/watches/uuid.jpg)
     */
    public String uploadWatchImage(MultipartFile file, String subfolder) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IOException("File rỗng!");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("File không phải là ảnh!");
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + extension;
        
        // Create upload directory if not exists (CHUNG folder watches/)
        String watchDir = uploadDir + File.separator + "watches";
        File watchDirFile = new File(watchDir);
        if (!watchDirFile.exists()) {
            watchDirFile.mkdirs();
        }
        
        // Read image
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        
        if (originalImage == null) {
            throw new IOException("Không thể đọc file ảnh!");
        }
        
        // Resize to 800x800 (giữ tỷ lệ)
        BufferedImage resizedImage = resizeImage(originalImage, TARGET_SIZE, TARGET_SIZE);
        
        // Save to disk
        File outputFile = new File(watchDir + File.separator + newFilename);
        String formatName = extension.substring(1).toLowerCase();
        if (formatName.equals("jpg")) {
            formatName = "jpeg";
        }
        ImageIO.write(resizedImage, formatName, outputFile);
        
        // Return filename only - templates will use @{'/uploads/watches/' + filename}
        return newFilename;
    }
    
    /**
     * Xóa watch image
     * @param imagePath Relative path from database (e.g., /uploads/watches/main/uuid.jpg)
     */
    public void deleteWatchImage(String imagePath) throws IOException {
        if (imagePath == null || imagePath.isEmpty()) {
            return;
        }
        
        // Convert relative path to file path
        // Remove leading "/" and replace with uploadDir
        String filePath = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
        Path path = Paths.get(uploadDir).getParent().resolve(filePath);
        
        Files.deleteIfExists(path);
    }
    
    /**
     * Xóa ảnh (method cũ)
     */
    public void deleteImage(String filename) {
        if (filename == null || filename.isEmpty()) {
            return;
        }
        
        try {
            Path filePath = Paths.get(uploadDir, filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Lỗi xóa file: " + e.getMessage());
        }
    }
}
