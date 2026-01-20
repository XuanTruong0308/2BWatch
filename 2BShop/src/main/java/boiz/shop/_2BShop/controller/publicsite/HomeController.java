package boiz.shop._2BShop.controller.publicsite;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import boiz.shop._2BShop.entity.Watch;
import boiz.shop._2BShop.service.WatchService;

/**
 * DEPRECATED: Chức năng trang chủ đã được chuyển sang PublicController
 * File này được giữ lại để tránh conflict, nhưng không sử dụng nữa.
 * 
 * @see boiz.shop._2BShop.controller.PublicController#homePage(Model)
 */
@Controller
public class HomeController {
    
    // All methods moved to PublicController.java
    // This file is deprecated and kept for reference only
}