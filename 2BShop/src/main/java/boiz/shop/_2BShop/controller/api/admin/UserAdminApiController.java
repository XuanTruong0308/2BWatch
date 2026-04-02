package boiz.shop._2BShop.controller.api.admin;

import boiz.shop._2BShop.controller.api.ApiDataMapper;
import boiz.shop._2BShop.controller.api.ApiValueParser;
import boiz.shop._2BShop.entity.Role;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.entity.UserRole;
import boiz.shop._2BShop.respository.OrderRepository;
import boiz.shop._2BShop.respository.RoleRepository;
import boiz.shop._2BShop.respository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Admin User API")
@RestController
@RequestMapping("/api/v1/admin/users")
public class UserAdminApiController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private RoleRepository roleRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Operation(summary = "Get users for admin")
    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive) {
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());

            Page<User> users;
            if (keyword != null && !keyword.trim().isEmpty()) {
                users = userRepo.searchUsers(keyword, pageable);
            } else if (isActive != null) {
                users = userRepo.findByIsEnabled(isActive, pageable);
            } else {
                users = userRepo.findAll(pageable);
            }

            List<Map<String, Object>> items = new ArrayList<>();
            for (User user : users.getContent()) {
                Map<String, Object> userData = new HashMap<>(ApiDataMapper.userSummary(user));
                long orderCount = orderRepo.countByUserUserId(user.getUserId());
                BigDecimal totalSpent = orderRepo.sumTotalAmountByUserUserId(user.getUserId());
                userData.put("orderCount", orderCount);
                userData.put("totalSpent", totalSpent != null ? totalSpent : BigDecimal.ZERO);
                items.add(userData);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("items", items);
            data.put("page", ApiDataMapper.pageInfo(users));
                Map<String, Object> filters = new HashMap<>();
                filters.put("keyword", keyword);
                filters.put("isActive", isActive);
                data.put("filters", filters);

            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi tải danh sách user: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Get user detail")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> detail(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user với id: " + id));

            var orders = orderRepo.findByUserUserId(id);
            long orderCount = orders.size();
            BigDecimal totalSpent = orders.stream()
                    .map(order -> order.getTotalAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> data = new HashMap<>();
            data.put("user", ApiDataMapper.userSummary(user));
            data.put("orders", ApiDataMapper.mapOrders(orders));
            data.put("orderCount", orderCount);
            data.put("totalSpent", totalSpent);

            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Ban user")
    @PatchMapping("/{id}/ban")
    public ResponseEntity<Map<String, Object>> ban(
            @PathVariable Integer id,
            @RequestBody(required = false) Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user với id"));

            boolean isAdmin = user.getUserRoles().stream()
                    .anyMatch(ur -> ur.getRole().getRoleName().equals("ADMIN"));

            if (isAdmin) {
                throw new RuntimeException("Không thể ban admin");
            }

            user.setIsEnabled(false);
            user.setUpdatedDate(LocalDateTime.now());
            userRepo.save(user);

            response.put("success", true);
            response.put("message", "Đã ban user thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Unban user")
    @PatchMapping("/{id}/unban")
    public ResponseEntity<Map<String, Object>> unban(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
            user.setIsEnabled(true);
            user.setUpdatedDate(LocalDateTime.now());
            userRepo.save(user);

            response.put("success", true);
            response.put("message", "Đã unban user thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Create user")
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> payload) {
        return saveUserInternal(null, payload);
    }

    @Operation(summary = "Update user")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> payload) {
        return saveUserInternal(id, payload);
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            User user = userRepo.findById(id).orElse(null);
            if (user == null) {
                throw new RuntimeException("Không tìm thấy user!");
            }

            boolean isAdmin = user.getUserRoles().stream()
                    .anyMatch(ur -> "ADMIN".equals(ur.getRole().getRoleName()));
            if (isAdmin) {
                throw new RuntimeException("Không thể xóa tài khoản Admin!");
            }

            userRepo.delete(user);
            response.put("success", true);
            response.put("message", "Xóa user thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi xóa: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private ResponseEntity<Map<String, Object>> saveUserInternal(Integer id, Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean isNew = (id == null);
            User user;

            if (isNew) {
                String username = ApiValueParser.asString(payload.get("username"));
                String email = ApiValueParser.asString(payload.get("email"));

                if (userRepo.existsByUsername(username)) {
                    throw new RuntimeException("Username đã tồn tại!");
                }
                if (userRepo.existsByEmail(email)) {
                    throw new RuntimeException("Email đã tồn tại!");
                }

                String newPassword = ApiValueParser.asString(payload.get("newPassword"));
                if (newPassword == null || newPassword.trim().isEmpty()) {
                    throw new RuntimeException("Vui lòng nhập password!");
                }

                user = new User();
                user.setUsername(username);
                user.setEmail(email);
                user.setPassword(passwordEncoder.encode(newPassword));
                user.setCreatedDate(LocalDateTime.now());
                user.setIsEnabled(true);
                user.setIsBanned(false);
            } else {
                user = userRepo.findById(id).orElse(null);
                if (user == null) {
                    throw new RuntimeException("User không tồn tại!");
                }

                String newPassword = ApiValueParser.asString(payload.get("newPassword"));
                if (newPassword != null && !newPassword.trim().isEmpty()) {
                    user.setPassword(passwordEncoder.encode(newPassword));
                }
            }

            if (payload.containsKey("username")) {
                user.setUsername(ApiValueParser.asString(payload.get("username")));
            }
            if (payload.containsKey("email")) {
                user.setEmail(ApiValueParser.asString(payload.get("email")));
            }
            if (payload.containsKey("fullName")) {
                user.setFullName(ApiValueParser.asString(payload.get("fullName")));
            }
            if (payload.containsKey("phone")) {
                user.setPhone(ApiValueParser.asString(payload.get("phone")));
            }
            if (payload.containsKey("address")) {
                user.setAddress(ApiValueParser.asString(payload.get("address")));
            }
            if (payload.containsKey("isEnabled")) {
                user.setIsEnabled(ApiValueParser.asBoolean(payload.get("isEnabled")));
            }

            user.setUpdatedDate(LocalDateTime.now());

            User savedUser = userRepo.save(user);
            applyRoles(savedUser, extractRoleNames(payload.get("roleNames")));

            response.put("success", true);
            response.put("message", isNew ? "Thêm user thành công!" : "Cập nhật user thành công!");
            response.put("data", ApiDataMapper.userSummary(savedUser));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private void applyRoles(User savedUser, List<String> roleNames) {
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
                UserRole role = new UserRole();
                role.setUser(savedUser);
                role.setRole(userRole);
                savedUser.getUserRoles().add(role);
            }
        }

        userRepo.save(savedUser);
    }

    private List<String> extractRoleNames(Object roleObj) {
        List<String> roleNames = new ArrayList<>();
        if (roleObj == null) {
            return roleNames;
        }

        if (roleObj instanceof List<?>) {
            for (Object item : (List<?>) roleObj) {
                if (item != null) {
                    roleNames.add(item.toString());
                }
            }
            return roleNames;
        }

        roleNames.add(roleObj.toString());
        return roleNames;
    }
}
