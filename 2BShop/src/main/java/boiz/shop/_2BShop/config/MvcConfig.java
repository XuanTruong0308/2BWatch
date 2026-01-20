package boiz.shop._2BShop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    // Lấy đường dẫn gốc: C:/uploads/bshop/
    @Value("${app.upload.dir}")
    String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        /**
         * Cấu hình này cực kỳ quan trọng:
         * Nó biến toàn bộ thư mục gốc C:/uploads/bshop/ thành URL /assets/images/
         * * Vì bạn đã có folder con /watch/ và /user/ bên trong, nên:
         * - Web gọi: /assets/images/watch/1.jpg -> Máy tính tìm: C:/uploads/bshop/watch/1.jpg
         * - Web gọi: /assets/images/user/ava.jpg -> Máy tính tìm: C:/uploads/bshop/user/ava.jpg
         */
        registry.addResourceHandler("/assets/images/**")
                .addResourceLocations("file:/" + uploadDir);
    }
}