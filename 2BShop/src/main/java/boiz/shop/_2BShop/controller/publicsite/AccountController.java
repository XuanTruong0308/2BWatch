package boiz.shop._2BShop.controller.publicsite;

import boiz.shop._2BShop.dto.RegisterDTO;
import boiz.shop._2BShop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * DEPRECATED: Tất cả chức năng authentication đã được chuyển sang PublicController
 * File này được giữ lại để tránh conflict, nhưng không sử dụng nữa.
 * 
 * @see boiz.shop._2BShop.controller.PublicController (Authentication section)
 */
@Controller
public class AccountController {
    
    // All methods moved to PublicController.java
    // This file is deprecated and kept for reference only
}
