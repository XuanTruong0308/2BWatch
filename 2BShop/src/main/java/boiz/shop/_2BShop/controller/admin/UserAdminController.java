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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import boiz.shop._2BShop.entity.Role;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.entity.UserRole;
import boiz.shop._2BShop.respository.OrderRepository;
import boiz.shop._2BShop.respository.RoleRepository;
import boiz.shop._2BShop.respository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class UserAdminController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    @GetMapping("/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", roleRepo.findAll());
        model.addAttribute("isEdit", false);
        return "admin/user-form";
    }

    @GetMapping("/{id}/edit")
    public String editUserForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        User user = userRepo.findById(id).orElse(null);
        
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy user!");
            return "redirect:/admin/users";
        }
        
        model.addAttribute("user", user);
        model.addAttribute("roles", roleRepo.findAll());
        model.addAttribute("isEdit", true);
        
        List<String> userRoleNames = new ArrayList<>();
        if (user.getUserRoles() != null) {
            for (UserRole ur : user.getUserRoles()) {
                userRoleNames.add(ur.getRole().getRoleName());
            }
        }
        model.addAttribute("userRoleNames", userRoleNames);
        
        return "admin/user-form";
    }

    @PostMapping("/save")
    public String saveUser(
            @ModelAttribute User user,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) List<String> roleNames,
            RedirectAttributes redirectAttributes) {
        try {
            boolean isNew = (user.getUserId() == null);
            
            if (isNew) {
                if (userRepo.existsByUsername(user.getUsername())) {
                    redirectAttributes.addFlashAttribute("error", "Username đã tồn tại!");
                    return "redirect:/admin/users/new";
                }
                
                if (userRepo.existsByEmail(user.getEmail())) {
                    redirectAttributes.addFlashAttribute("error", "Email đã tồn tại!");
                    return "redirect:/admin/users/new";
                }
                
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    user.setPassword(passwordEncoder.encode(newPassword));
                } else {
                    redirectAttributes.addFlashAttribute("error", "Vui lòng nhập password!");
                    return "redirect:/admin/users/new";
                }
                
                user.setCreatedDate(LocalDateTime.now());
                user.setIsEnabled(true);
                user.setIsBanned(false);
            } else {
                User existingUser = userRepo.findById(user.getUserId()).orElse(null);
                if (existingUser == null) {
                    redirectAttributes.addFlashAttribute("error", "User không tồn tại!");
                    return "redirect:/admin/users";
                }
                
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    user.setPassword(passwordEncoder.encode(newPassword));
                } else {
                    user.setPassword(existingUser.getPassword());
                }
                
                user.setCreatedDate(existingUser.getCreatedDate());
                user.setIsBanned(existingUser.getIsBanned());
            }
            
            user.setUpdatedDate(LocalDateTime.now());
            
            User savedUser = userRepo.save(user);
            
            if (savedUser.getUserRoles() != null) {
                savedUser.getUserRoles().clear();
            } else {
                savedUser.setUserRoles(new ArrayList<>());
            }
            
            if (roleNames != null && !roleNames.isEmpty()) {
                for (String roleName : roleNames) {
                    Role role = roleRepo.findByRoleName(roleName).orElse(null);
                    if (role != null) {
                        UserRole userRole = new UserRole();
                        userRole.setUser(savedUser);
                        userRole.setRole(role);
                        savedUser.getUserRoles().add(userRole);
                    }
                }
            } else {
                Role userRole = roleRepo.findByRoleName("USER").orElse(null);
                if (userRole != null) {
                    UserRole ur = new UserRole();
                    ur.setUser(savedUser);
                    ur.setRole(userRole);
                    savedUser.getUserRoles().add(ur);
                }
            }
            
            userRepo.save(savedUser);
            
            String message = isNew ? "Thêm user thành công!" : "Cập nhật user thành công!";
            redirectAttributes.addFlashAttribute("success", message);
            return "redirect:/admin/users";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            User user = userRepo.findById(id).orElse(null);
            
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy user!");
                return "redirect:/admin/users";
            }
            
            boolean isAdmin = user.getUserRoles().stream()
                    .anyMatch(ur -> "ADMIN".equals(ur.getRole().getRoleName()));
            
            if (isAdmin) {
                redirectAttributes.addFlashAttribute("error", "Không thể xóa tài khoản Admin!");
                return "redirect:/admin/users";
            }
            
            userRepo.delete(user);
            redirectAttributes.addFlashAttribute("success", "Xóa user thành công!");
            return "redirect:/admin/users";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }
}
