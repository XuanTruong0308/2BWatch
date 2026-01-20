package boiz.shop._2BShop.controller.usersite;

import boiz.shop._2BShop.entity.Cart;
import boiz.shop._2BShop.entity.CartItem;
import boiz.shop._2BShop.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DEPRECATED: Tất cả chức năng giỏ hàng đã được chuyển sang UserController
 * File này được giữ lại để tránh conflict, nhưng không sử dụng nữa.
 * 
 * @see boiz.shop._2BShop.controller.UserController (Cart Management section)
 */
@Controller
@RequestMapping("/old-cart")
public class CartController {
    
    // All methods moved to UserController.java
    // This file is deprecated and kept for reference only
}
