package boiz.shop._2BShop.controller;

import boiz.shop._2BShop.entity.BankAccount;
import boiz.shop._2BShop.entity.CartItem;
import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.respository.UserRepository;
import boiz.shop._2BShop.service.BankAccountService;
import boiz.shop._2BShop.service.CartService;
import boiz.shop._2BShop.service.CheckoutService;
import boiz.shop._2BShop.service.CheckoutService.CheckoutSummary;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private CartService cartService;

    @Autowired
    private BankAccountService bankAccountService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String checkoutPage(Model model,
            @RequestParam(required = false) String couponCode) {
        // 1. Get current user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElse(null);

        // 2. Check cart
        List<CartItem> cartItems = cartService.getSelectedCartItems();
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        // 3. Calculate totals
        CheckoutSummary summary = checkoutService.calculateTotals(couponCode);

        // 4. Get bank accounts
        List<BankAccount> bankAccounts = bankAccountService.getActiveBankAccounts();

        // 5. Add attributes
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("summary", summary);
        model.addAttribute("bankAccounts", bankAccounts);
        model.addAttribute("user", user);

        return "public/checkout";
    }

    @PostMapping("/place-order")
    public String placeOrder(@RequestParam String receiverName,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam String paymentMethod,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) String couponCode,
            @RequestParam(required = false) Integer bankAccountId,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email).orElseThrow();

            Order order = checkoutService.placeOrder(user, receiverName, phone, address, notes, paymentMethod,
                    couponCode, bankAccountId);

            // Show success modal on checkout page (no redirect)
            model.addAttribute("orderSuccess", true);
            model.addAttribute("orderId", order.getOrderId());
            model.addAttribute("orderCode", "ORD" + String.format("%06d", order.getOrderId()));
            model.addAttribute("customerEmail", user.getEmail());
            
            return "public/checkout";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/confirmation/{orderId}")
    public String orderConfirmation(@PathVariable Integer orderId, Model model, RedirectAttributes redirectAttributes) {
        try {
            // Get current user
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email).orElseThrow(() -> 
                new RuntimeException("Không tìm thấy thông tin người dùng!"));

            // Get order and verify ownership
            Order order = checkoutService.getOrderById(orderId);
            if (!order.getUser().getUserId().equals(user.getUserId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền xem đơn hàng này!");
                return "redirect:/";
            }

            model.addAttribute("orderId", orderId);
            model.addAttribute("order", order);
            return "public/order-confirmation";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Không thể tải trang xác nhận: " + e.getMessage());
            return "redirect:/";
        }
    }
}
