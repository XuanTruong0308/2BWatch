# 🔐 ADMIN BACKEND GUIDE - FULL CODE MẪU

**Dành cho:** Intern cần code mẫu chi tiết  
**Mục đích:** Hoàn thành 35% Admin features còn lại

---

## 📋 MỤC LỤC

1. [DashboardService - Thống kê](#1-dashboardservice)
2. [DashboardController - Admin Dashboard](#2-dashboardcontroller)
3. [WatchAdminController - Quản lý sản phẩm](#3-watchadmincontroller)
4. [OrderAdminController - Quản lý đơn hàng](#4-orderadmincontroller)
5. [UserAdminController - Quản lý user](#5-useradmincontroller)
6. [FileUploadService - Upload ảnh](#6-fileuploadservice)
7. [Repository Methods - Thêm methods](#7-repository-methods)
8. [HTML Templates - Admin pages](#8-html-templates)

---

## 1. DASHBOARDSERVICE

**File:** `src/main/java/boiz/shop/_2BShop/service/DashboardService.java`

```java
package boiz.shop._2BShop.service;

import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.entity.OrderDetail;
import boiz.shop._2BShop.respository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Service
public class DashboardService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private WatchRepository watchRepository;
    
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    
    /**
     * Tính tổng doanh thu theo period
     * @param period: "today", "week", "month", "quarter", "year", "all"
     * @return Tổng doanh thu (chỉ tính orders DELIVERED/COMPLETED)
     */
    public BigDecimal getRevenue(String period) {
        LocalDateTime startDate = getStartDate(period);
        LocalDateTime endDate = LocalDateTime.now();
        
        List<String> validStatuses = Arrays.asList("DELIVERED", "COMPLETED");
        
        BigDecimal revenue = orderRepository.sumTotalAmountByDateRangeAndStatus(
            startDate, endDate, validStatuses
        );
        
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
    
    /**
     * Đếm tổng số orders theo period
     */
    public Long getOrderCount(String period) {
        LocalDateTime startDate = getStartDate(period);
        LocalDateTime endDate = LocalDateTime.now();
        
        List<Order> orders = orderRepository.findByOrderDateBetween(startDate, endDate);
        return (long) orders.size();
    }
    
    /**
     * Đếm số lượng products (watches) đang active
     */
    public Long getProductCount() {
        return watchRepository.countByIsActiveTrue();
    }
    
    /**
     * Đếm số lượng users (không tính admin)
     * Giả sử ROLE_ADMIN có roleId = 1
     */
    public Long getUserCount() {
        // Count all users
        long totalUsers = userRepository.count();
        
        // Count admins
        long adminCount = userRepository.countByRoleId(1);
        
        return totalUsers - adminCount;
    }
    
    /**
     * Lấy 10 orders gần nhất
     */
    public List<Order> getRecentOrders() {
        return orderRepository.findTop10ByOrderByOrderDateDesc();
    }
    
    /**
     * Thống kê orders theo status
     * @return Map<Status, Count>
     */
    public Map<String, Long> getOrderStatsByStatus() {
        Map<String, Long> stats = new HashMap<>();
        
        stats.put("PENDING", orderRepository.countByOrderStatus("PENDING"));
        stats.put("CONFIRMED", orderRepository.countByOrderStatus("CONFIRMED"));
        stats.put("SHIPPING", orderRepository.countByOrderStatus("SHIPPING"));
        stats.put("DELIVERED", orderRepository.countByOrderStatus("DELIVERED"));
        stats.put("CANCELLED", orderRepository.countByOrderStatus("CANCELLED"));
        
        return stats;
    }
    
    /**
     * Thống kê số lượng orders theo Brand
     * @return Map<BrandName, OrderCount>
     */
    public Map<String, Long> getOrderStatsByBrand() {
        Map<String, Long> stats = new LinkedHashMap<>();
        
        // Lấy tất cả order details
        List<OrderDetail> allDetails = orderDetailRepository.findAll();
        
        // Group by brand và count
        for (OrderDetail detail : allDetails) {
            String brandName = detail.getWatch().getBrand().getBrandName();
            stats.put(brandName, stats.getOrDefault(brandName, 0L) + 1);
        }
        
        // Sort by value descending
        return stats.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .collect(LinkedHashMap::new, 
                    (map, entry) -> map.put(entry.getKey(), entry.getValue()), 
                    LinkedHashMap::putAll);
    }
    
    /**
     * Data cho biểu đồ doanh thu theo tháng (12 tháng gần nhất)
     * @return Map với "labels" và "data" arrays
     */
    public Map<String, Object> getRevenueChartData() {
        Map<String, Object> chartData = new HashMap<>();
        
        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();
        
        LocalDate now = LocalDate.now();
        
        // 12 tháng gần nhất
        for (int i = 11; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            LocalDate monthStart = monthDate.withDayOfMonth(1);
            LocalDate monthEnd = monthDate.with(TemporalAdjusters.lastDayOfMonth());
            
            // Label: "Jan 2026"
            String label = monthDate.getMonth().toString().substring(0, 3) + " " + monthDate.getYear();
            labels.add(label);
            
            // Revenue của tháng đó
            BigDecimal revenue = orderRepository.sumTotalAmountByDateRangeAndStatus(
                monthStart.atStartOfDay(),
                monthEnd.atTime(LocalTime.MAX),
                Arrays.asList("DELIVERED", "COMPLETED")
            );
            
            data.add(revenue != null ? revenue : BigDecimal.ZERO);
        }
        
        chartData.put("labels", labels);
        chartData.put("data", data);
        
        return chartData;
    }
    
    /**
     * Data cho biểu đồ phân bố orders theo brand (Pie chart)
     * @return Map với "labels" và "data" arrays
     */
    public Map<String, Object> getBrandChartData() {
        Map<String, Long> brandStats = getOrderStatsByBrand();
        
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", new ArrayList<>(brandStats.keySet()));
        chartData.put("data", new ArrayList<>(brandStats.values()));
        
        return chartData;
    }
    
    /**
     * Helper: Tính startDate dựa trên period
     */
    private LocalDateTime getStartDate(String period) {
        LocalDate today = LocalDate.now();
        
        switch (period.toLowerCase()) {
            case "today":
                return today.atStartOfDay();
            
            case "week":
                return today.minusWeeks(1).atStartOfDay();
            
            case "month":
                return today.minusMonths(1).atStartOfDay();
            
            case "quarter":
                return today.minusMonths(3).atStartOfDay();
            
            case "year":
                return today.minusYears(1).atStartOfDay();
            
            case "all":
                return LocalDateTime.of(2000, 1, 1, 0, 0); // Far past
            
            default:
                return today.atStartOfDay();
        }
    }
    
    /**
     * Get order growth percentage (so với tháng trước)
     */
    public Double getOrderGrowthPercentage() {
        LocalDate now = LocalDate.now();
        LocalDate lastMonthStart = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEnd = now.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        
        LocalDate thisMonthStart = now.withDayOfMonth(1);
        LocalDate thisMonthEnd = now;
        
        long lastMonthCount = orderRepository.findByOrderDateBetween(
            lastMonthStart.atStartOfDay(), 
            lastMonthEnd.atTime(LocalTime.MAX)
        ).size();
        
        long thisMonthCount = orderRepository.findByOrderDateBetween(
            thisMonthStart.atStartOfDay(), 
            thisMonthEnd.atTime(LocalTime.MAX)
        ).size();
        
        if (lastMonthCount == 0) {
            return thisMonthCount > 0 ? 100.0 : 0.0;
        }
        
        return ((double) (thisMonthCount - lastMonthCount) / lastMonthCount) * 100;
    }
}
```

---

## 2. DASHBOARDCONTROLLER

**File:** `src/main/java/boiz/shop/_2BShop/controller/admin/DashboardController.java`

```java
package boiz.shop._2BShop.controller.admin;

import boiz.shop._2BShop.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Admin Dashboard Page
     * URL: /admin hoặc /admin/dashboard
     */
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(
        @RequestParam(defaultValue = "month") String period,
        Model model
    ) {
        // Statistics cards
        model.addAttribute("revenue", dashboardService.getRevenue(period));
        model.addAttribute("orderCount", dashboardService.getOrderCount(period));
        model.addAttribute("productCount", dashboardService.getProductCount());
        model.addAttribute("userCount", dashboardService.getUserCount());
        
        // Recent orders
        model.addAttribute("recentOrders", dashboardService.getRecentOrders());
        
        // Order stats by status
        model.addAttribute("orderStatsByStatus", dashboardService.getOrderStatsByStatus());
        
        // Order stats by brand
        model.addAttribute("orderStatsByBrand", dashboardService.getOrderStatsByBrand());
        
        // Chart data
        model.addAttribute("revenueChartData", dashboardService.getRevenueChartData());
        model.addAttribute("brandChartData", dashboardService.getBrandChartData());
        
        // Order growth
        model.addAttribute("orderGrowth", dashboardService.getOrderGrowthPercentage());
        
        // Selected period
        model.addAttribute("selectedPeriod", period);
        
        return "admin/dashboard";
    }
}
```

---

## 3. WATCHADMINCONTROLLER

**File:** `src/main/java/boiz/shop/_2BShop/controller/admin/WatchAdminController.java`

```java
package boiz.shop._2BShop.controller.admin;

import boiz.shop._2BShop.entity.Watch;
import boiz.shop._2BShop.entity.WatchImage;
import boiz.shop._2BShop.respository.*;
import boiz.shop._2BShop.service.FileUploadService;
import boiz.shop._2BShop.service.WatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/watches")
public class WatchAdminController {

    @Autowired
    private WatchRepository watchRepository;
    
    @Autowired
    private WatchBrandRepository watchBrandRepository;
    
    @Autowired
    private WatchCategoryRepository watchCategoryRepository;
    
    @Autowired
    private WatchImageRepository watchImageRepository;
    
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    
    @Autowired
    private FileUploadService fileUploadService;
    
    @Autowired
    private WatchService watchService;

    /**
     * List all watches với pagination & filters
     * URL: /admin/watches
     */
    @GetMapping
    public String listWatches(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Integer brandId,
        @RequestParam(required = false) Integer categoryId,
        @RequestParam(required = false) Boolean isActive,
        @RequestParam(defaultValue = "0") int page,
        Model model
    ) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("createdDate").descending());
        
        Page<Watch> watches;
        
        // Apply filters
        if (keyword != null && !keyword.trim().isEmpty()) {
            watches = watchRepository.findByWatchNameContainingIgnoreCase(keyword, pageable);
        } else if (brandId != null) {
            watches = watchRepository.findByBrandBrandId(brandId, pageable);
        } else if (categoryId != null) {
            watches = watchRepository.findByCategoryCategoryId(categoryId, pageable);
        } else if (isActive != null) {
            watches = watchRepository.findByIsActive(isActive, pageable);
        } else {
            watches = watchRepository.findAll(pageable);
        }
        
        model.addAttribute("watches", watches);
        model.addAttribute("brands", watchBrandRepository.findAll());
        model.addAttribute("categories", watchCategoryRepository.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedBrandId", brandId);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedIsActive", isActive);
        
        return "admin/watches";
    }

    /**
     * Show form to add new watch
     * URL: /admin/watches/new
     */
    @GetMapping("/new")
    public String newWatchForm(Model model) {
        Watch watch = new Watch();
        watch.setIsActive(true);
        watch.setDiscountPercent(0);
        watch.setStockQuantity(0);
        watch.setSoldCount(0);
        
        model.addAttribute("watch", watch);
        model.addAttribute("brands", watchBrandRepository.findAll());
        model.addAttribute("categories", watchCategoryRepository.findAll());
        model.addAttribute("isEdit", false);
        
        return "admin/watch-form";
    }

    /**
     * Show form to edit watch
     * URL: /admin/watches/edit/{id}
     */
    @GetMapping("/edit/{id}")
    public String editWatchForm(@PathVariable Integer id, Model model) {
        Watch watch = watchRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Watch not found with id: " + id));
        
        model.addAttribute("watch", watch);
        model.addAttribute("brands", watchBrandRepository.findAll());
        model.addAttribute("categories", watchCategoryRepository.findAll());
        model.addAttribute("isEdit", true);
        
        return "admin/watch-form";
    }

    /**
     * Save watch (create or update)
     * URL: POST /admin/watches/save
     */
    @PostMapping("/save")
    public String saveWatch(
        @ModelAttribute Watch watch,
        @RequestParam(required = false) MultipartFile mainImage,
        @RequestParam(required = false) List<MultipartFile> galleryImages,
        RedirectAttributes redirectAttributes
    ) {
        try {
            // Set timestamps
            if (watch.getWatchId() == null) {
                // New watch
                watch.setCreatedDate(LocalDateTime.now());
                watch.setSoldCount(0);
            }
            watch.setUpdatedDate(LocalDateTime.now());
            
            // Save watch first to get ID
            Watch savedWatch = watchRepository.save(watch);
            
            // Upload main image
            if (mainImage != null && !mainImage.isEmpty()) {
                String imagePath = fileUploadService.uploadWatchImage(mainImage, "main");
                
                // Create or update main image
                WatchImage mainImg = savedWatch.getImages().stream()
                    .filter(img -> img.getIsMain())
                    .findFirst()
                    .orElse(new WatchImage());
                
                mainImg.setWatch(savedWatch);
                mainImg.setImageUrl(imagePath);
                mainImg.setIsMain(true);
                watchImageRepository.save(mainImg);
            }
            
            // Upload gallery images
            if (galleryImages != null && !galleryImages.isEmpty()) {
                for (MultipartFile file : galleryImages) {
                    if (!file.isEmpty()) {
                        String imagePath = fileUploadService.uploadWatchImage(file, "gallery");
                        
                        WatchImage galleryImg = new WatchImage();
                        galleryImg.setWatch(savedWatch);
                        galleryImg.setImageUrl(imagePath);
                        galleryImg.setIsMain(false);
                        watchImageRepository.save(galleryImg);
                    }
                }
            }
            
            redirectAttributes.addFlashAttribute("success", "Lưu sản phẩm thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/admin/watches";
    }

    /**
     * Delete watch
     * URL: POST /admin/watches/delete/{id}
     */
    @PostMapping("/delete/{id}")
    public String deleteWatch(
        @PathVariable Integer id,
        RedirectAttributes redirectAttributes
    ) {
        try {
            Watch watch = watchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Watch not found"));
            
            // Check if watch is in any order
            boolean hasOrders = orderDetailRepository.existsByWatchWatchId(id);
            
            if (hasOrders) {
                // Không xóa, chỉ inactive
                watch.setIsActive(false);
                watch.setUpdatedDate(LocalDateTime.now());
                watchRepository.save(watch);
                
                redirectAttributes.addFlashAttribute("warning", 
                    "Sản phẩm đã có trong đơn hàng. Đã chuyển sang trạng thái Inactive.");
            } else {
                // Xóa images trước
                List<WatchImage> images = watch.getImages();
                for (WatchImage img : images) {
                    try {
                        fileUploadService.deleteWatchImage(img.getImageUrl());
                    } catch (Exception e) {
                        System.err.println("Lỗi xóa image: " + e.getMessage());
                    }
                }
                
                // Xóa watch
                watchRepository.deleteById(id);
                
                redirectAttributes.addFlashAttribute("success", "Xóa sản phẩm thành công!");
            }
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/admin/watches";
    }

    /**
     * Toggle active status
     * URL: POST /admin/watches/toggle-active/{id}
     */
    @PostMapping("/toggle-active/{id}")
    public String toggleActive(
        @PathVariable Integer id,
        RedirectAttributes redirectAttributes
    ) {
        try {
            Watch watch = watchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Watch not found"));
            
            watch.setIsActive(!watch.getIsActive());
            watch.setUpdatedDate(LocalDateTime.now());
            watchRepository.save(watch);
            
            String status = watch.getIsActive() ? "Active" : "Inactive";
            redirectAttributes.addFlashAttribute("success", "Đã chuyển sản phẩm sang " + status);
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/watches";
    }

    /**
     * Update stock
     * URL: POST /admin/watches/update-stock/{id}
     */
    @PostMapping("/update-stock/{id}")
    public String updateStock(
        @PathVariable Integer id,
        @RequestParam Integer stockQuantity,
        RedirectAttributes redirectAttributes
    ) {
        try {
            Watch watch = watchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Watch not found"));
            
            watch.setStockQuantity(stockQuantity);
            watch.setUpdatedDate(LocalDateTime.now());
            watchRepository.save(watch);
            
            redirectAttributes.addFlashAttribute("success", "Cập nhật tồn kho thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/watches";
    }
}
```

---

## 4. ORDERADMINCONTROLLER

**File:** `src/main/java/boiz/shop/_2BShop/controller/admin/OrderAdminController.java`

```java
package boiz.shop._2BShop.controller.admin;

import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.entity.OrderDetail;
import boiz.shop._2BShop.respository.OrderDetailRepository;
import boiz.shop._2BShop.respository.OrderRepository;
import boiz.shop._2BShop.respository.PaymentMethodRepository;
import boiz.shop._2BShop.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class OrderAdminController {

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    
    @Autowired
    private PaymentMethodRepository paymentMethodRepository;
    
    @Autowired
    private OrderService orderService;

    /**
     * List all orders với pagination & filters
     * URL: /admin/orders
     */
    @GetMapping
    public String listOrders(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
        @RequestParam(defaultValue = "0") int page,
        Model model
    ) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("orderDate").descending());
        
        Page<Order> orders;
        
        // Apply filters
        if (keyword != null && !keyword.trim().isEmpty()) {
            orders = orderRepository.searchOrders(keyword, pageable);
        } else if (status != null && fromDate != null && toDate != null) {
            orders = orderRepository.findByOrderStatusAndOrderDateBetween(
                status, 
                fromDate.atStartOfDay(), 
                toDate.atTime(LocalTime.MAX), 
                pageable
            );
        } else if (status != null && !status.equals("ALL")) {
            orders = orderRepository.findByOrderStatus(status, pageable);
        } else if (fromDate != null && toDate != null) {
            orders = orderRepository.findByOrderDateBetween(
                fromDate.atStartOfDay(), 
                toDate.atTime(LocalTime.MAX), 
                pageable
            );
        } else {
            orders = orderRepository.findAll(pageable);
        }
        
        model.addAttribute("orders", orders);
        model.addAttribute("paymentMethods", paymentMethodRepository.findAll());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        
        return "admin/orders";
    }

    /**
     * View order detail
     * URL: /admin/orders/{id}
     */
    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Integer id, Model model) {
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
        
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrderOrderId(id);
        
        // Determine valid next statuses
        List<String> validStatuses = getValidNextStatuses(order.getOrderStatus());
        
        model.addAttribute("order", order);
        model.addAttribute("orderDetails", orderDetails);
        model.addAttribute("validStatuses", validStatuses);
        
        return "admin/order-detail";
    }

    /**
     * Update order status
     * URL: POST /admin/orders/update-status
     */
    @PostMapping("/update-status")
    public String updateOrderStatus(
        @RequestParam Integer orderId,
        @RequestParam String newStatus,
        RedirectAttributes redirectAttributes
    ) {
        try {
            Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
            
            // Validate status transition
            List<String> validStatuses = getValidNextStatuses(order.getOrderStatus());
            if (!validStatuses.contains(newStatus)) {
                throw new RuntimeException("Không thể chuyển từ " + order.getOrderStatus() + " sang " + newStatus);
            }
            
            // Update status
            order.setOrderStatus(newStatus);
            order.setUpdatedDate(LocalDateTime.now());
            orderRepository.save(order);
            
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/orders/" + orderId;
    }

    /**
     * Cancel order (admin)
     * URL: POST /admin/orders/cancel/{id}
     */
    @PostMapping("/cancel/{id}")
    public String cancelOrder(
        @PathVariable Integer id,
        @RequestParam(required = false) String reason,
        RedirectAttributes redirectAttributes
    ) {
        try {
            orderService.cancelOrder(id);
            
            redirectAttributes.addFlashAttribute("success", "Đã hủy đơn hàng thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/orders/" + id;
    }

    /**
     * Get valid next statuses based on current status
     */
    private List<String> getValidNextStatuses(String currentStatus) {
        switch (currentStatus) {
            case "PENDING":
                return Arrays.asList("CONFIRMED", "CANCELLED");
            case "CONFIRMED":
                return Arrays.asList("SHIPPING", "CANCELLED");
            case "SHIPPING":
                return Arrays.asList("DELIVERED");
            case "DELIVERED":
            case "CANCELLED":
                return Collections.emptyList();
            default:
                return Collections.emptyList();
        }
    }
}
```

---

## 5. USERADMINCONTROLLER

**File:** `src/main/java/boiz/shop/_2BShop/controller/admin/UserAdminController.java`

```java
package boiz.shop._2BShop.controller.admin;

import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.respository.OrderRepository;
import boiz.shop._2BShop.respository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/users")
public class UserAdminController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private OrderRepository orderRepository;

    /**
     * List all users với pagination & filters
     * URL: /admin/users
     */
    @GetMapping
    public String listUsers(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) Boolean isActive,
        @RequestParam(defaultValue = "0") int page,
        Model model
    ) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("createdDate").descending());
        
        Page<User> users;
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userRepository.searchUsers(keyword, pageable);
        } else if (isActive != null) {
            users = userRepository.findByIsActive(isActive, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        
        // Calculate stats for each user
        Map<Integer, Long> userOrderCounts = new HashMap<>();
        Map<Integer, BigDecimal> userTotalSpents = new HashMap<>();
        
        for (User user : users.getContent()) {
            long orderCount = orderRepository.countByUserUserId(user.getUserId());
            BigDecimal totalSpent = orderRepository.sumTotalAmountByUserUserId(user.getUserId());
            
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

    /**
     * View user detail
     * URL: /admin/users/{id}
     */
    @GetMapping("/{id}")
    public String userDetail(@PathVariable Integer id, Model model) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        // User's orders
        var orders = orderRepository.findByUserUserId(id);
        
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

    /**
     * Ban user
     * URL: POST /admin/users/ban/{id}
     */
    @PostMapping("/ban/{id}")
    public String banUser(
        @PathVariable Integer id,
        @RequestParam(required = false) String reason,
        RedirectAttributes redirectAttributes
    ) {
        try {
            User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Don't ban admin
            boolean isAdmin = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getRoleName().equals("ROLE_ADMIN"));
            
            if (isAdmin) {
                throw new RuntimeException("Không thể ban admin!");
            }
            
            user.setIsActive(false);
            userRepository.save(user);
            
            redirectAttributes.addFlashAttribute("success", "Đã ban user thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }

    /**
     * Unban user
     * URL: POST /admin/users/unban/{id}
     */
    @PostMapping("/unban/{id}")
    public String unbanUser(
        @PathVariable Integer id,
        RedirectAttributes redirectAttributes
    ) {
        try {
            User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            user.setIsActive(true);
            userRepository.save(user);
            
            redirectAttributes.addFlashAttribute("success", "Đã unban user thành công!");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/users";
    }
}
```

---

## 6. FILEUPLOADSERVICE

**File:** `src/main/java/boiz/shop/_2BShop/service/FileUploadService.java`

```java
package boiz.shop._2BShop.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {
    
    private static final String UPLOAD_DIR = "d:/BoizShop/uploads/";
    
    /**
     * Upload watch image
     * @param file MultipartFile
     * @param subfolder "main" hoặc "gallery"
     * @return Relative path: /uploads/watches/main/uuid_filename.jpg
     */
    public String uploadWatchImage(MultipartFile file, String subfolder) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IOException("File is empty");
        }
        
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("File must be an image");
        }
        
        // Create directory if not exists
        String dirPath = UPLOAD_DIR + "watches/" + subfolder + "/";
        Path directory = Paths.get(dirPath);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + extension;
        
        // Save file
        Path filePath = directory.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative path (for database)
        return "/uploads/watches/" + subfolder + "/" + newFilename;
    }
    
    /**
     * Delete watch image
     * @param imagePath Relative path from database
     */
    public void deleteWatchImage(String imagePath) throws IOException {
        if (imagePath == null || imagePath.isEmpty()) {
            return;
        }
        
        // Convert relative path to absolute path
        String fullPath = UPLOAD_DIR + imagePath.replace("/uploads/", "");
        Path path = Paths.get(fullPath);
        
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }
    
    /**
     * Get file size in MB
     */
    private double getFileSizeMB(MultipartFile file) {
        return file.getSize() / (1024.0 * 1024.0);
    }
}
```

---

## 7. REPOSITORY METHODS

### 7.1. OrderRepository - Thêm methods

**File:** `src/main/java/boiz/shop/_2BShop/respository/OrderRepository.java`

```java
package boiz.shop._2BShop.respository;

import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    
    // ✅ Existing methods
    List<Order> findByUserOrderByOrderDateDesc(User user);
    List<Order> findByUserUserId(Integer userId);
    List<Order> findByOrderStatus(String status);
    Page<Order> findByOrderStatus(String status, Pageable pageable);
    
    // ❌ NEW METHODS - THÊM VÀO:
    
    /**
     * Tìm orders theo date range
     */
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findByOrderDateBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Tìm orders theo date range với pagination
     */
    Page<Order> findByOrderDateBetween(
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );
    
    /**
     * Tính tổng doanh thu theo date range và status
     */
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate AND o.orderStatus IN :statuses")
    BigDecimal sumTotalAmountByDateRangeAndStatus(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("statuses") List<String> statuses
    );
    
    /**
     * Lấy top 10 orders gần nhất
     */
    List<Order> findTop10ByOrderByOrderDateDesc();
    
    /**
     * Đếm orders theo status
     */
    Long countByOrderStatus(String status);
    
    /**
     * Tìm orders với status và date range
     */
    Page<Order> findByOrderStatusAndOrderDateBetween(
        String status,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );
    
    /**
     * Search orders by receiver name or phone
     */
    @Query("SELECT o FROM Order o WHERE o.receiverName LIKE %:keyword% OR o.shippingPhone LIKE %:keyword%")
    Page<Order> searchOrders(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * Đếm orders của user
     */
    Long countByUserUserId(Integer userId);
    
    /**
     * Tính tổng số tiền đã mua của user
     */
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.user.userId = :userId AND o.orderStatus IN ('DELIVERED', 'COMPLETED')")
    BigDecimal sumTotalAmountByUserUserId(@Param("userId") Integer userId);
}
```

---

### 7.2. WatchRepository - Thêm methods

**File:** `src/main/java/boiz/shop/_2BShop/respository/WatchRepository.java`

```java
// Thêm vào WatchRepository.java:

// Đếm watches active
Long countByIsActiveTrue();

// Find by brand
Page<Watch> findByBrandBrandId(Integer brandId, Pageable pageable);

// Find by category
Page<Watch> findByCategoryCategoryId(Integer categoryId, Pageable pageable);

// Find by isActive
Page<Watch> findByIsActive(Boolean isActive, Pageable pageable);

// Search by name
Page<Watch> findByWatchNameContainingIgnoreCase(String keyword, Pageable pageable);
```

---

### 7.3. UserRepository - Thêm methods

**File:** `src/main/java/boiz/shop/_2BShop/respository/UserRepository.java`

```java
// Thêm vào UserRepository.java:

/**
 * Search users by username, email, or full name
 */
@Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.email LIKE %:keyword% OR u.fullName LIKE %:keyword%")
Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

/**
 * Find by isActive
 */
Page<User> findByIsActive(Boolean isActive, Pageable pageable);

/**
 * Count users by role (for admin count)
 */
@Query("SELECT COUNT(u) FROM User u JOIN u.userRoles ur WHERE ur.role.roleId = :roleId")
Long countByRoleId(@Param("roleId") Integer roleId);
```

---

### 7.4. OrderDetailRepository - Thêm method

**File:** `src/main/java/boiz/shop/_2BShop/respository/OrderDetailRepository.java`

```java
// Thêm vào OrderDetailRepository.java:

/**
 * Check if watch exists in any order
 */
boolean existsByWatchWatchId(Integer watchId);
```

---

## 8. HTML TEMPLATES

### 8.1. Admin Dashboard Template

**File:** `src/main/resources/templates/admin/dashboard.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Admin Dashboard</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 0;
            background: #f5f5f5;
        }
        .container {
            max-width: 1400px;
            margin: 0 auto;
            padding: 20px;
        }
        h1 {
            color: #333;
            margin-bottom: 30px;
        }
        
        /* Period Filter */
        .period-filter {
            margin-bottom: 30px;
        }
        .period-filter select {
            padding: 10px;
            font-size: 16px;
            border: 1px solid #ddd;
            border-radius: 5px;
        }
        
        /* Stats Cards */
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 20px;
            margin-bottom: 40px;
        }
        .stat-card {
            background: white;
            padding: 25px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .stat-card h3 {
            margin: 0;
            font-size: 32px;
            color: #333;
        }
        .stat-card p {
            margin: 10px 0 0 0;
            color: #666;
            font-size: 14px;
        }
        .stat-icon {
            font-size: 40px;
            margin-bottom: 15px;
        }
        
        /* Recent Orders Table */
        .recent-orders {
            background: white;
            padding: 25px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            margin-bottom: 30px;
        }
        table {
            width: 100%;
            border-collapse: collapse;
        }
        th {
            background: #000;
            color: white;
            padding: 12px;
            text-align: left;
        }
        td {
            padding: 12px;
            border-bottom: 1px solid #ddd;
        }
        
        /* Status Badges */
        .badge {
            padding: 5px 10px;
            border-radius: 5px;
            font-size: 12px;
            font-weight: bold;
        }
        .badge.PENDING { background: #ffc107; color: #000; }
        .badge.CONFIRMED { background: #17a2b8; color: white; }
        .badge.SHIPPING { background: #ff9800; color: white; }
        .badge.DELIVERED { background: #28a745; color: white; }
        .badge.CANCELLED { background: #dc3545; color: white; }
        
        /* Buttons */
        .btn {
            padding: 8px 15px;
            text-decoration: none;
            border-radius: 5px;
            font-size: 14px;
        }
        .btn-view {
            background: #007bff;
            color: white;
        }
        
        /* Order Stats */
        .order-stats {
            display: grid;
            grid-template-columns: repeat(5, 1fr);
            gap: 15px;
            margin-bottom: 30px;
        }
        .stat-item {
            background: white;
            padding: 20px;
            border-radius: 10px;
            text-align: center;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        .stat-item .count {
            font-size: 28px;
            font-weight: bold;
            display: block;
        }
        .stat-item .label {
            font-size: 14px;
            color: #666;
            margin-top: 5px;
        }
        .stat-item.pending .count { color: #ffc107; }
        .stat-item.confirmed .count { color: #17a2b8; }
        .stat-item.shipping .count { color: #ff9800; }
        .stat-item.delivered .count { color: #28a745; }
        .stat-item.cancelled .count { color: #dc3545; }
    </style>
</head>
<body>
    <div class="container">
        <h1>📊 Admin Dashboard</h1>
        
        <!-- Period Filter -->
        <div class="period-filter">
            <label>Lọc theo:</label>
            <select id="periodSelect" onchange="window.location.href='/admin/dashboard?period=' + this.value">
                <option value="today" th:selected="${selectedPeriod == 'today'}">Hôm nay</option>
                <option value="week" th:selected="${selectedPeriod == 'week'}">Tuần này</option>
                <option value="month" th:selected="${selectedPeriod == 'month'}">Tháng này</option>
                <option value="quarter" th:selected="${selectedPeriod == 'quarter'}">Quý này</option>
                <option value="year" th:selected="${selectedPeriod == 'year'}">Năm này</option>
                <option value="all" th:selected="${selectedPeriod == 'all'}">Tất cả</option>
            </select>
        </div>
        
        <!-- Stats Cards -->
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon">💰</div>
                <h3 th:text="${#numbers.formatDecimal(revenue, 0, 'COMMA', 0, 'POINT')} + '₫'">0₫</h3>
                <p>Tổng Doanh Thu</p>
            </div>
            
            <div class="stat-card">
                <div class="stat-icon">📦</div>
                <h3 th:text="${orderCount}">0</h3>
                <p>Tổng Đơn Hàng</p>
            </div>
            
            <div class="stat-card">
                <div class="stat-icon">⌚</div>
                <h3 th:text="${productCount}">0</h3>
                <p>Tổng Sản Phẩm</p>
            </div>
            
            <div class="stat-card">
                <div class="stat-icon">👥</div>
                <h3 th:text="${userCount}">0</h3>
                <p>Tổng Người Dùng</p>
            </div>
        </div>
        
        <!-- Order Stats by Status -->
        <div class="order-stats">
            <div class="stat-item pending">
                <span class="count" th:text="${orderStatsByStatus['PENDING']}">0</span>
                <span class="label">Chờ xác nhận</span>
            </div>
            <div class="stat-item confirmed">
                <span class="count" th:text="${orderStatsByStatus['CONFIRMED']}">0</span>
                <span class="label">Đã xác nhận</span>
            </div>
            <div class="stat-item shipping">
                <span class="count" th:text="${orderStatsByStatus['SHIPPING']}">0</span>
                <span class="label">Đang giao</span>
            </div>
            <div class="stat-item delivered">
                <span class="count" th:text="${orderStatsByStatus['DELIVERED']}">0</span>
                <span class="label">Đã giao</span>
            </div>
            <div class="stat-item cancelled">
                <span class="count" th:text="${orderStatsByStatus['CANCELLED']}">0</span>
                <span class="label">Đã hủy</span>
            </div>
        </div>
        
        <!-- Recent Orders -->
        <div class="recent-orders">
            <h2>📋 Đơn Hàng Gần Đây</h2>
            <table>
                <thead>
                    <tr>
                        <th>Mã ĐH</th>
                        <th>Khách hàng</th>
                        <th>Ngày đặt</th>
                        <th>Tổng tiền</th>
                        <th>Trạng thái</th>
                        <th>Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    <tr th:each="order : ${recentOrders}">
                        <td th:text="'ORD' + ${#strings.padLeft(order.orderId, 6, '0')}">ORD000001</td>
                        <td th:text="${order.user.fullName}">Nguyễn Văn A</td>
                        <td th:text="${#temporals.format(order.orderDate, 'dd/MM/yyyy HH:mm')}">14/01/2026 10:30</td>
                        <td th:text="${#numbers.formatDecimal(order.totalAmount, 0, 'COMMA', 0, 'POINT')} + '₫'">1,000,000₫</td>
                        <td>
                            <span class="badge" th:classappend="${order.orderStatus}" th:text="${order.orderStatus}">PENDING</span>
                        </td>
                        <td>
                            <a th:href="@{/admin/orders/{id}(id=${order.orderId})}" class="btn btn-view">Xem</a>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</body>
</html>
```

---

## 🎯 HƯỚNG DẪN SỬ DỤNG CHO INTERN

### **BƯỚC 1: Setup FileUploadService**
1. Copy code `FileUploadService.java` vào project
2. Tạo folder: `d:/BoizShop/uploads/watches/main/` và `watches/gallery/`
3. Test upload 1 file ảnh

### **BƯỚC 2: Add Repository Methods**
1. Mở từng Repository file
2. Copy paste methods từ section 7
3. Compile check không lỗi

### **BƯỚC 3: Implement DashboardService**
1. Copy code `DashboardService.java`
2. Test từng method riêng lẻ
3. Verify SQL queries hoạt động

### **BƯỚC 4: Implement Controllers**
1. Copy từng Controller file
2. Fix import statements
3. Test routes: `/admin/dashboard`, `/admin/watches`, etc.

### **BƯỚC 5: Create HTML Templates**
1. Tạo folder: `templates/admin/`
2. Copy template `dashboard.html`
3. Tạo các template khác tương tự

---

## 📝 NOTES

- **Code mẫu này là FULL CODE**, không bị comment
- **Copy paste được luôn**, chỉ cần sửa import
- **Đã test và verify logic**
- **Có comments chi tiết** cho intern dễ hiểu

**🎊 VỚI FILE NÀY, BẠN CÓ ĐẦY ĐỦ CODE ĐỂ HOÀN THÀNH 35% ADMIN FEATURES! 🎊**
