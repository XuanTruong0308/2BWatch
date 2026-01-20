package boiz.shop._2BShop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration để serve static files từ external folder
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${app.upload.dir}")
    private String uploadDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /uploads/** URLs to external folder d:/BoizShop/uploads/
        // Ví dụ: http://localhost:8080/uploads/watches/main/abc.jpg
        // sẽ serve file từ d:/BoizShop/uploads/watches/main/abc.jpg
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///" + uploadDir + "/");
    }
}
