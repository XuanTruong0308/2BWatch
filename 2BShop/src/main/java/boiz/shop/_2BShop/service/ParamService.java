package boiz.shop._2BShop.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;

@Service
public class ParamService {
    @Autowired HttpServletRequest request;

    @Value("${app.upload.dir}")
    String rootPath;

    @Value("${app.upload.watch}")
    String watchSubPath; // /watch/

    @Value("${app.upload.user}")
    String userSubPath; // /user/

    // Hàm lưu ảnh đồng hồ (Tự động lưu vào C:/uploads/bshop/watch/)
    public File saveWatchImage(MultipartFile file) {
        return this.save(file, rootPath + watchSubPath);
    }

    // Hàm lưu ảnh người dùng (Tự động lưu vào C:/uploads/bshop/user/)
    public File saveUserImage(MultipartFile file) {
        return this.save(file, rootPath + userSubPath);
    }

    // Hàm gốc để lưu file
    public File save(MultipartFile file, String path) {
        if (!file.isEmpty()) {
            File dir = new File(path);
            if (!dir.exists()) dir.mkdirs();
            try {
                String name = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                File savedFile = new File(dir, name);
                file.transferTo(savedFile);
                return savedFile;
            } catch (Exception e) { throw new RuntimeException(e); }
        }
        return null;
    }
}