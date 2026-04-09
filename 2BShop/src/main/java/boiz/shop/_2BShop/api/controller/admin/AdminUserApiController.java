package boiz.shop._2BShop.api.controller.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import boiz.shop._2BShop.api.mapper.ApiMapper;
import boiz.shop._2BShop.api.request.UserUpsertRequest;
import boiz.shop._2BShop.api.response.ApiResponse;
import boiz.shop._2BShop.api.response.PaginatedResponse;
import boiz.shop._2BShop.entity.Role;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.entity.UserRole;
import boiz.shop._2BShop.respository.OrderRepository;
import boiz.shop._2BShop.respository.RoleRepository;
import boiz.shop._2BShop.respository.UserRepository;
import boiz.shop._2BShop.respository.UserRoleRepository;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserApiController {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApiMapper apiMapper;

    public AdminUserApiController(
            UserRepository userRepository,
            OrderRepository orderRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder,
            ApiMapper apiMapper) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.roleRepository = roleRepository;
        this.userRoleRepository = userRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.apiMapper = apiMapper;
    }

    @GetMapping
    public PaginatedResponse<?> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("createdDate").descending());
        Page<User> users;
        if (keyword != null && !keyword.isBlank()) {
            users = userRepository.searchUsers(keyword, pageable);
        } else if (isActive != null) {
            users = userRepository.findByIsEnabled(isActive, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return PaginatedResponse.from(users, user -> apiMapper.toUserDto(
                user,
                orderRepository.countByUserUserId(user.getUserId()),
                defaultAmount(orderRepository.sumTotalAmountByUserUserId(user.getUserId()))));
    }

    @GetMapping("/options")
    public ApiResponse<Map<String, ?>> options() {
        return ApiResponse.success(Map.of(
                "roles", roleRepository.findAll().stream().map(role -> Map.of(
                        "id", role.getRoleId(),
                        "value", role.getRoleName(),
                        "label", role.getRoleName())).toList()));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> detail(@PathVariable Integer id) {
        User user = findUser(id);
        return ApiResponse.success(apiMapper.toUserDto(
                user,
                orderRepository.countByUserUserId(user.getUserId()),
                defaultAmount(orderRepository.sumTotalAmountByUserUserId(user.getUserId()))));
    }

    @PostMapping
    public ApiResponse<?> create(@RequestBody UserUpsertRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("Username đã tồn tại");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email đã tồn tại");
        }
        if (request.newPassword() == null || request.newPassword().isBlank()) {
            throw new RuntimeException("Vui lòng nhập password");
        }
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setAddress(request.address());
        user.setAvatarUrl(request.avatarUrl());
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setIsEnabled(request.enabled() == null ? true : request.enabled());
        user.setIsBanned(false);
        user.setCreatedDate(LocalDateTime.now());
        user.setUpdatedDate(LocalDateTime.now());
        User saved = userRepository.save(user);
        syncRoles(saved, request.roleNames());
        return ApiResponse.success("Thêm user thành công", apiMapper.toUserDto(
                userRepository.findById(saved.getUserId()).orElse(saved),
                0L,
                BigDecimal.ZERO));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Integer id, @RequestBody UserUpsertRequest request) {
        User user = findUser(id);
        if (!user.getUsername().equalsIgnoreCase(request.username()) && userRepository.existsByUsername(request.username())) {
            throw new RuntimeException("Username đã tồn tại");
        }
        if (!user.getEmail().equalsIgnoreCase(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email đã tồn tại");
        }
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setAddress(request.address());
        user.setAvatarUrl(request.avatarUrl());
        user.setIsEnabled(request.enabled() == null ? user.getIsEnabled() : request.enabled());
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.newPassword()));
        }
        user.setUpdatedDate(LocalDateTime.now());
        User saved = userRepository.save(user);
        syncRoles(saved, request.roleNames());
        return ApiResponse.success("Cập nhật user thành công", apiMapper.toUserDto(
                userRepository.findById(saved.getUserId()).orElse(saved),
                orderRepository.countByUserUserId(saved.getUserId()),
                defaultAmount(orderRepository.sumTotalAmountByUserUserId(saved.getUserId()))));
    }

    @PostMapping("/{id}/ban")
    public ApiResponse<?> ban(@PathVariable Integer id, @RequestParam(required = false) String reason) {
        User user = findUser(id);
        if (isAdmin(user)) {
            throw new RuntimeException("Không thể ban admin");
        }
        user.setIsEnabled(false);
        user.setUpdatedDate(LocalDateTime.now());
        return ApiResponse.success("Đã ban user thành công", apiMapper.toUserDto(userRepository.save(user), 0L, BigDecimal.ZERO));
    }

    @PostMapping("/{id}/unban")
    public ApiResponse<?> unban(@PathVariable Integer id) {
        User user = findUser(id);
        user.setIsEnabled(true);
        user.setUpdatedDate(LocalDateTime.now());
        return ApiResponse.success("Đã mở khóa user thành công", apiMapper.toUserDto(userRepository.save(user), 0L, BigDecimal.ZERO));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Integer id) {
        User user = findUser(id);
        if (isAdmin(user)) {
            throw new RuntimeException("Không thể xóa tài khoản Admin");
        }
        userRepository.delete(user);
        return ApiResponse.success("Xóa user thành công", null);
    }

    private User findUser(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
    }

    private void syncRoles(User user, List<String> roleNames) {
        userRoleRepository.deleteByUserUserId(user.getUserId());
        List<String> finalRoles = roleNames == null || roleNames.isEmpty() ? List.of("USER") : roleNames;
        List<UserRole> userRoles = new ArrayList<>();
        for (String roleName : finalRoles) {
            Role role = roleRepository.findByRoleName(roleName)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy role: " + roleName));
            userRoles.add(new UserRole(null, user, role));
        }
        user.setUserRoles(userRoles);
        userRepository.save(user);
    }

    private boolean isAdmin(User user) {
        return user.getUserRoles() != null
                && user.getUserRoles().stream().anyMatch(userRole -> "ADMIN".equals(userRole.getRole().getRoleName()));
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
