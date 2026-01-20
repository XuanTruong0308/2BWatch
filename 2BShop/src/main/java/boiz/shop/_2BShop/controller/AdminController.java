// package boiz.shop._2BShop.controller;

// import java.math.BigDecimal;
// import java.text.NumberFormat;
// import java.util.List;
// import java.util.Locale;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.domain.Sort;
// import org.springframework.stereotype.Controller;
// import org.springframework.ui.Model;
// import org.springframework.web.bind.annotation.*;
// import org.springframework.web.multipart.MultipartFile;
// import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// import boiz.shop._2BShop.entity.Order;
// import boiz.shop._2BShop.entity.User;
// import boiz.shop._2BShop.entity.Watch;
// import boiz.shop._2BShop.respository.OrderRepository;
// import boiz.shop._2BShop.respository.UserRepository;
// import boiz.shop._2BShop.respository.WatchRepository;
// import boiz.shop._2BShop.service.WatchService;
// import boiz.shop._2BShop.service.FileUploadService;
// import boiz.shop._2BShop.service.CartService;
// import boiz.shop._2BShop.service.UserService;
// import boiz.shop._2BShop.service.OrderService;

// /**
//  * ========================================
//  * ADMIN CONTROLLER - TẤT CẢ CHỨC NĂNG ADMIN
//  * ========================================
//  * Bao gồm:
//  * 1. Dashboard - Thống kê tổng quan
//  * 2. Watch Management - Quản lý đồng hồ
//  * 3. Order Management - Quản lý đơn hàng
//  * 4. User Management - Quản lý người dùng (ban/unban)
//  * 5. Payment Management - Quản lý thanh toán
//  */
// @Controller
// @RequestMapping("/admin")
// public class AdminController {
    
//     @Autowired
//     private OrderRepository orderRepo;
    
//     @Autowired
//     private WatchRepository watchRepo;
    
//     @Autowired
//     private UserRepository userRepo;
    
//     @Autowired
//     private WatchService watchService;
    
//     @Autowired
//     private FileUploadService fileUploadService;
    
//     @Autowired
//     private CartService cartService;
    
//     @Autowired
//     private UserService userService;
    
//     @Autowired
//     private OrderService orderService;
    
//     // ========================================
//     // 1. DASHBOARD - THỐNG KÊ TỔNG QUAN
//     // ========================================
    
//     @GetMapping({"", "/", "/dashboard"})
//     public String dashboard(Model model) {
//         NumberFormat vndFormat = NumberFormat.getCurrencyInstance(Locale.of("vi", "VN"));
        
//         // 1. Tổng doanh thu (chỉ tính đơn hàng DELIVERED/COMPLETED)
//         BigDecimal totalRevenue = orderRepo.sumTotalAmountByStatus("DELIVERED");
//         BigDecimal completedRevenue = orderRepo.sumTotalAmountByStatus("COMPLETED");
        
//         if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;
//         if (completedRevenue != null) totalRevenue = totalRevenue.add(completedRevenue);
        
//         String formattedRevenue = vndFormat.format(totalRevenue);
//         model.addAttribute("totalRevenue", formattedRevenue);
//         model.addAttribute("revenueValue", totalRevenue);
        
//         // 2. Tổng đơn hàng
//         Long totalOrders = orderRepo.count();
//         model.addAttribute("totalOrders", totalOrders);
        
//         // 3. Đơn hàng chờ xử lý (PENDING)
//         Long pendingOrders = orderRepo.countByStatus("PENDING");
//         model.addAttribute("pendingOrders", pendingOrders != null ? pendingOrders : 0);
        
//         // 4. Tổng sản phẩm
//         Long totalProducts = watchRepo.count();
//         model.addAttribute("totalProducts", totalProducts);
        
//         // 5. Tổng người dùng (chỉ tính USER, không tính ADMIN)
//         Long totalUsers = userRepo.count();
//         model.addAttribute("totalUsers", totalUsers);
        
//         // 6. 10 đơn hàng gần đây
//         List<Order> recentOrders = orderRepo.findTop10ByOrderByOrderDateDesc();
//         model.addAttribute("recentOrders", recentOrders);
        
//         // 7. Thống kê theo trạng thái
//         Long confirmedOrders = orderRepo.countByStatus("CONFIRMED");
//         Long shippingOrders = orderRepo.countByStatus("SHIPPING");
//         Long deliveredOrders = orderRepo.countByStatus("DELIVERED");
//         Long cancelledOrders = orderRepo.countByStatus("CANCELLED");
        
//         model.addAttribute("confirmedOrders", confirmedOrders != null ? confirmedOrders : 0);
//         model.addAttribute("shippingOrders", shippingOrders != null ? shippingOrders : 0);
//         model.addAttribute("deliveredOrders", deliveredOrders != null ? deliveredOrders : 0);
//         model.addAttribute("cancelledOrders", cancelledOrders != null ? cancelledOrders : 0);
        
//         return "admin/dashboard";
//     }
    
//     // ========================================
//     // 2. WATCH MANAGEMENT - QUẢN LÝ ĐỒNG HỒ
//     // ========================================
    
//     /**
//      * Danh sách đồng hồ với search, filter, pagination
//      */
//     @GetMapping("/watches")
//     public String listWatches(
//         Model model,
//         @RequestParam(required = false) String search,
//         @RequestParam(required = false) String brand,
//         @RequestParam(required = false) String status,
//         @RequestParam(defaultValue = "0") int page,
//         @RequestParam(defaultValue = "10") int size
//     ) {
//         try {
//             Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
//             Page<Watch> watchPage;
            
//             // Filter logic
//             if (search != null && !search.trim().isEmpty()) {
//                 watchPage = watchService.search(search, pageable);
//                 model.addAttribute("search", search);
//             } else if (brand != null && !brand.trim().isEmpty()) {
//                 watchPage = watchService.findByBrand(brand, pageable);
//                 model.addAttribute("brand", brand);
//             } else if ("instock".equals(status)) {
//                 watchPage = watchService.findByStockStatus(true, pageable);
//                 model.addAttribute("status", status);
//             } else if ("outofstock".equals(status)) {
//                 watchPage = watchService.findByStockStatus(false, pageable);
//                 model.addAttribute("status", status);
//             } else {
//                 watchPage = watchService.findAll(pageable);
//             }
            
//             model.addAttribute("watches", watchPage.getContent());
//             model.addAttribute("currentPage", page);
//             model.addAttribute("totalPages", watchPage.getTotalPages());
//             model.addAttribute("totalItems", watchPage.getTotalElements());
            
//         } catch (Exception e) {
//             model.addAttribute("error", "Lỗi khi tải danh sách: " + e.getMessage());
//         }
        
//         return "admin/watches";
//     }
    
//     /**
//      * Hiển thị form thêm đồng hồ mới
//      */
//     @GetMapping("/watches/new")
//     public String showCreateWatchForm(Model model) {
//         model.addAttribute("watch", new Watch());
//         return "admin/watch-form";
//     }
    
//     /**
//      * Xử lý thêm đồng hồ mới
//      */
//     @PostMapping("/watches/create")
//     public String createWatch(
//         @ModelAttribute Watch watch,
//         @RequestParam("imageFile") MultipartFile imageFile,
//         RedirectAttributes redirectAttributes
//     ) {
//         try {
//             // Validate
//             if (watch.getPrice() == null || watch.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
//                 throw new RuntimeException("Giá phải lớn hơn 0");
//             }
            
//             if (watch.getStockQuantity() == null || watch.getStockQuantity() < 0) {
//                 throw new RuntimeException("Số lượng không hợp lệ");
//             }
            
//             // Upload ảnh (TODO: Implement WatchImage entity handling)
//             // if (imageFile != null && !imageFile.isEmpty()) {
//             //     String imageUrl = fileUploadService.uploadImage(imageFile);
//             //     // Create WatchImage entity and add to watch.images list
//             // }
            
//             // Save watch
//             watchService.save(watch);
            
//             redirectAttributes.addFlashAttribute("success", 
//                 "Thêm đồng hồ thành công! Sản phẩm đã được hiển thị trên trang chủ.");
//             return "redirect:/admin/watches";
            
//         } catch (Exception e) {
//             redirectAttributes.addFlashAttribute("error", 
//                 "Lỗi khi thêm đồng hồ: " + e.getMessage());
//             return "redirect:/admin/watches/new";
//         }
//     }
    
//     /**
//      * Hiển thị form sửa đồng hồ
//      */
//     @GetMapping("/watches/edit/{id}")
//     public String showEditWatchForm(@PathVariable Integer id, Model model,
//                                     RedirectAttributes redirectAttributes) {
//         try {
//             Watch watch = watchService.findById(id)
//                 .orElseThrow(() -> new RuntimeException("Không tìm thấy đồng hồ với ID: " + id));
            
//             model.addAttribute("watch", watch);
//             return "admin/watch-form";
            
//         } catch (Exception e) {
//             redirectAttributes.addFlashAttribute("error", e.getMessage());
//             return "redirect:/admin/watches";
//         }
//     }
    
//     /**
//      * Xử lý cập nhật đồng hồ
//      * QUAN TRỌNG: Xử lý stock = 0 → xóa khỏi giỏ hàng
//      */
//     @PostMapping("/watches/update/{id}")
//     public String updateWatch(
//         @PathVariable Integer id,
//         @ModelAttribute Watch watch,
//         @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
//         RedirectAttributes redirectAttributes
//     ) {
//         try {
//             // Tìm watch hiện tại
//             Watch existingWatch = watchService.findById(id)
//                 .orElseThrow(() -> new RuntimeException("Không tìm thấy đồng hồ với ID: " + id));
            
//             // Cập nhật thông tin
//             existingWatch.setWatchName(watch.getWatchName());
//             existingWatch.setBrand(watch.getBrand());
//             existingWatch.setDescription(watch.getDescription());
//             existingWatch.setPrice(watch.getPrice());
//             existingWatch.setDiscountPercent(watch.getDiscountPercent());
//             existingWatch.setStockQuantity(watch.getStockQuantity());
//             existingWatch.setCategory(watch.getCategory());
            
//             // Nếu có upload ảnh mới (TODO: Implement WatchImage entity handling)
//             // if (imageFile != null && !imageFile.isEmpty()) {
//             //     String newImageUrl = fileUploadService.uploadImage(imageFile);
//             //     // Create WatchImage entity and add to watch.images list
//             // }
            
//             // LOGIC QUAN TRỌNG: Xử lý khi stock = 0
//             if (existingWatch.getStockQuantity() == 0) {
//                 // Xóa sản phẩm khỏi giỏ hàng của tất cả users
//                 cartService.removeItemsForWatch(id);
//             }
            
//             // Save
//             watchService.save(existingWatch);
            
//             redirectAttributes.addFlashAttribute("success", "Cập nhật đồng hồ thành công!");
//             return "redirect:/admin/watches";
            
//         } catch (Exception e) {
//             redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
//             return "redirect:/admin/watches/edit/" + id;
//         }
//     }
    
//     /**
//      * Xóa đồng hồ
//      */
//     @PostMapping("/watches/delete/{id}")
//     public String deleteWatch(@PathVariable Integer id,
//                              RedirectAttributes redirectAttributes) {
//         try {
//             // Tìm watch
//             Watch watch = watchService.findById(id)
//                 .orElseThrow(() -> new RuntimeException("Không tìm thấy đồng hồ với ID: " + id));
            
//             // Xóa ảnh (TODO: Implement WatchImage entity handling)
//             // if (watch.getImages() != null && !watch.getImages().isEmpty()) {
//             //     for (WatchImage img : watch.getImages()) {
//             //         fileUploadService.deleteImage(img.getImageUrl());
//             //     }
//             // }
            
//             // Xóa khỏi tất cả giỏ hàng
//             cartService.removeItemsForWatch(id);
            
//             // Xóa watch
//             watchService.delete(id);
            
//             redirectAttributes.addFlashAttribute("success", "Xóa đồng hồ thành công!");
//             return "redirect:/admin/watches";
            
//         } catch (Exception e) {
//             redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
//             return "redirect:/admin/watches";
//         }
//     }
    
//     // ========================================
//     // 3. ORDER MANAGEMENT - QUẢN LÝ ĐỐN HÀNG
//     // ========================================
    
//     /**
//      * Danh sách đơn hàng với filter theo status
//      */
//     @GetMapping("/orders")
//     public String listOrders(
//         Model model,
//         @RequestParam(required = false) String status,
//         @RequestParam(defaultValue = "0") int page,
//         @RequestParam(defaultValue = "20") int size
//     ) {
//         try {
//             Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
//             Page<Order> orderPage;
            
//             if (status != null && !status.trim().isEmpty()) {
//                 orderPage = orderService.findByStatus(status, pageable);
//                 model.addAttribute("selectedStatus", status);
//             } else {
//                 orderPage = orderService.findAll(pageable);
//             }
            
//             model.addAttribute("orders", orderPage.getContent());
//             model.addAttribute("currentPage", page);
//             model.addAttribute("totalPages", orderPage.getTotalPages());
            
//         } catch (Exception e) {
//             model.addAttribute("error", "Lỗi khi tải danh sách đơn hàng: " + e.getMessage());
//         }
        
//         return "admin/orders";
//     }
    
//     /**
//      * Chi tiết đơn hàng
//      */
//     @GetMapping("/orders/{id}")
//     public String orderDetail(@PathVariable Integer id, Model model,
//                              RedirectAttributes redirectAttributes) {
//         try {
//             Order order = orderService.findById(id)
//                 .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + id));
            
//             model.addAttribute("order", order);
//             return "admin/order-detail";
            
//         } catch (Exception e) {
//             redirectAttributes.addFlashAttribute("error", e.getMessage());
//             return "redirect:/admin/orders";
//         }
//     }
    
//     /**
//      * Cập nhật trạng thái đơn hàng
//      */
//     @PostMapping("/orders/{id}/update-status")
//     public String updateOrderStatus(
//         @PathVariable Integer id,
//         @RequestParam String status,
//         @RequestParam(required = false) String note,
//         RedirectAttributes redirectAttributes
//     ) {
//         try {
//             orderService.updateStatus(id, status, note);
            
//             redirectAttributes.addFlashAttribute("success", 
//                 "Cập nhật trạng thái đơn hàng thành công!");
//             return "redirect:/admin/orders/" + id;
            
//         } catch (Exception e) {
//             redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
//             return "redirect:/admin/orders/" + id;
//         }
//     }
    
//     // ========================================
//     // 4. USER MANAGEMENT - QUẢN LÝ NGƯỜI DÙNG
//     // ========================================
    
//     /**
//      * Danh sách người dùng
//      */
//     @GetMapping("/users")
//     public String listUsers(
//         Model model,
//         @RequestParam(required = false) String search,
//         @RequestParam(required = false) String role,
//         @RequestParam(defaultValue = "0") int page,
//         @RequestParam(defaultValue = "20") int size
//     ) {
//         try {
//             Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
//             Page<User> userPage;
            
//             if (search != null && !search.trim().isEmpty()) {
//                 userPage = userService.search(search, pageable);
//                 model.addAttribute("search", search);
//             } else if (role != null && !role.trim().isEmpty()) {
//                 userPage = userService.findByRole(role, pageable);
//                 model.addAttribute("role", role);
//             } else {
//                 userPage = userService.findAll(pageable);
//             }
            
//             model.addAttribute("users", userPage.getContent());
//             model.addAttribute("currentPage", page);
//             model.addAttribute("totalPages", userPage.getTotalPages());
            
//         } catch (Exception e) {
//             model.addAttribute("error", "Lỗi khi tải danh sách người dùng: " + e.getMessage());
//         }
        
//         return "admin/users";
//     }
    
//     /**
//      * Chi tiết người dùng
//      */
//     @GetMapping("/users/{id}")
//     public String userDetail(@PathVariable Integer id, Model model,
//                             RedirectAttributes redirectAttributes) {
//         try {
//             User user = userService.findById(id)
//                 .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + id));
            
//             model.addAttribute("user", user);
//             return "admin/user-detail";
            
//         } catch (Exception e) {
//             redirectAttributes.addFlashAttribute("error", e.getMessage());
//             return "redirect:/admin/users";
//         }
//     }
    
//     /**
//      * Ban người dùng
//      */
//     @PostMapping("/users/{id}/ban")
//     public String banUser(
//         @PathVariable Integer id,
//         @RequestParam String reason,
//         @RequestParam(required = false) Integer violationTypeId,
//         RedirectAttributes redirectAttributes
//     ) {
//         try {
//             userService.banUser(id, reason, violationTypeId);
            
//             redirectAttributes.addFlashAttribute("success", "Ban người dùng thành công!");
//             return "redirect:/admin/users/" + id;
            
//         } catch (Exception e) {
//             redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
//             return "redirect:/admin/users/" + id;
//         }
//     }
    
//     /**
//      * Unban người dùng
//      */
//     @PostMapping("/users/{id}/unban")
//     public String unbanUser(@PathVariable Integer id,
//                            RedirectAttributes redirectAttributes) {
//         try {
//             userService.unbanUser(id);
            
//             redirectAttributes.addFlashAttribute("success", "Unban người dùng thành công!");
//             return "redirect:/admin/users/" + id;
            
//         } catch (Exception e) {
//             redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
//             return "redirect:/admin/users/" + id;
//         }
//     }
    
//     // ========================================
//     // 5. PAYMENT MANAGEMENT - QUẢN LÝ THANH TOÁN
//     // ========================================
    
//     /**
//      * Danh sách giao dịch thanh toán
//      */
//     @GetMapping("/payments")
//     public String listPayments(Model model) {
//         // TODO: Implement payment management
//         return "admin/payments";
//     }
// }
