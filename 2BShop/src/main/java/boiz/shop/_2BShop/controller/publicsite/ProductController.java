package boiz.shop._2BShop.controller.publicsite;

import boiz.shop._2BShop.entity.Watch;
import boiz.shop._2BShop.service.WatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * DEPRECATED: Tất cả chức năng product listing đã được chuyển sang PublicController
 * File này được giữ lại để tránh conflict, nhưng không sử dụng nữa.
 * 
 * @see boiz.shop._2BShop.controller.PublicController (Product Listing & Detail section)
 */
@Controller
@RequestMapping("/old-watches")
public class ProductController {
    
    // All methods moved to PublicController.java
    // This file is deprecated and kept for reference only
}
