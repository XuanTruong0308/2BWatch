package boiz.shop._2BShop.controller.admin;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.respository.OrderRepository;
import boiz.shop._2BShop.respository.UserRepository;

@Controller
@RequestMapping("/admin/users")
public class UserAdminController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private OrderRepository orderRepo;

    @GetMapping
    public String listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("createdDate").descending());

        Page<User> users;

        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userRepo.searchUsers(keyword, pageable);
        } else if (isActive != null) {
            users = userRepo.findByIsEnabled(isActive, pageable);
        } else {
            users = userRepo.findAll(pageable);
        }

        Map<Integer, Long> userOrderCounts = new HashMap<>();
        Map<Integer, BigDecimal> userTotalSpents = new HashMap<>();

        for (User user : users.getContent()) {
            long orderCount = orderRepo.countByUserUserId(user.getUserId());
            BigDecimal totalSpent = orderRepo.sumTotalAmountByUserUserId(user.getUserId());

            userOrderCounts.put(user.getUserId(), orderCount);
            userTotalSpents.put(user.getUserId(), totalSpent != null ? totalSpent : BigDecimal.ZERO);
        }

        model.addAttribute("users", users);
        model.addAttribute("userOrderCounts", userOrderCounts);
        model.addAttribute("userTotalSpents", userTotalSpents);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedIsActive", isActive);

        return "admin/users";
    }

    @GetMapping("/{id}")
    public String userDetail(
            @PathVariable Integer id,
            Model model) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với id: " + id));

        var orders = orderRepo.findByUserUserId(id);

        // Stats
        long orderCount = orders.size();
        BigDecimal totalSpent = orders.stream()
                .map(order -> order.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("user", user);
        model.addAttribute("orders", orders);
        model.addAttribute("orderCount", orderCount);
        model.addAttribute("totalSpent", totalSpent);

        return "admin/user-detail";
    }

    @PostMapping("/ban/{id}")
    public String banUser(
            @PathVariable Integer id,
            @RequestParam(required = false) String reason,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user với id"));

            boolean isAdmin = user.getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole().getRoleName().equals("ADMIN"));

            if (isAdmin) {
                throw new RuntimeException("Không thể ban admin");
            }

            user.setIsEnabled(false);
            userRepo.save(user);

            redirectAttributes.addFlashAttribute("success", "Đã ban user thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/unban/{id}")
    public String unbanUser(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
            user.setIsEnabled(true);
            userRepo.save(user);

            redirectAttributes.addFlashAttribute("success", "Đã unban user thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
