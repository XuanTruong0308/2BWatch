# ❌ CÁC TASK CHƯA HOÀN THÀNH - 2BSHOP

**Ngày cập nhật:** 14/01/2026  
**Tổng tiến độ:** 35% chưa hoàn thành

---

## 🎯 PRIORITY LEVELS

- 🔴 **CRITICAL** - Phải làm ngay để hệ thống hoạt động đầy đủ
- 🟡 **HIGH** - Quan trọng, nên làm sớm
- 🟢 **MEDIUM** - Có thể làm sau
- ⚪ **LOW** - Optional, tính năng mở rộng

---

## 🔴 CRITICAL PRIORITY

### ❌ 1. UNCOMMENT & FIX ADMINCONTROLLER.JAVA

**File:** `d:\BoizShop\2BShop\src\main\java\boiz\shop\_2BShop\controller\AdminController.java`  
**Trạng thái:** Toàn bộ 511 dòng bị comment out  
**Ước tính:** 2 giờ

#### Tasks:
- [ ] Uncomment toàn bộ file (511 dòng)
- [ ] Fix tất cả import statements
- [ ] Check dependencies:
  - `DashboardService` (hiện đang bị comment)
  - `WatchService`
  - `OrderService`
  - `UserService`
  - All repositories
- [ ] Test compilation không có lỗi
- [ ] Test routes hoạt động:
  - `GET /admin` hoặc `/admin/dashboard`
  - `GET /admin/watches`
  - `GET /admin/watches/new`
  - `GET /admin/watches/edit/{id}`
  - `POST /admin/watches/save`
  - `DELETE /admin/watches/delete/{id}`
  - `GET /admin/orders`
  - `GET /admin/orders/{id}`
  - `POST /admin/orders/update-status`
  - `GET /admin/users`
  - `POST /admin/users/ban/{id}`
  - `POST /admin/users/unban/{id}`

#### Các lỗi có thể gặp:
1. **Missing DashboardService** → Cần uncomment & fix DashboardService.java
2. **Missing FileUploadService** → Cần tạo service này để upload ảnh sản phẩm
3. **Missing DTOs** → Có thể cần tạo WatchDTO cho form binding
4. **Missing repositories methods** → Xem task #3

---

### ❌ 2. FIX ORDERSERVICE BUG - PAYMENT TRANSACTION NOT SAVED

**File:** `d:\BoizShop\2BShop\src\main\java\boiz\shop\_2BShop\service\OrderService.java`  
**Line:** 158-161  
**Vấn đề:** PaymentTransaction được tạo nhưng KHÔNG save vào database  
**Impact:** Không có dữ liệu thanh toán trong bảng `payment_transactions`  
**Ước tính:** 15 phút

#### Current Code (Line 158-161):
```java
PaymentTransaction paymentTransaction = new PaymentTransaction();
paymentTransaction.setOrder(order);
paymentTransaction.setPaymentMethod(paymentMethod);
paymentTransaction.setAmount(order.getTotalAmount());
// ❌ BUG: Không có save() ở đây!
```

#### Fix Required:
```java
// Thêm @Autowired ở đầu class
@Autowired
private PaymentTransactionRepository paymentTransactionRepository;

// Sau line 161, thêm:
paymentTransaction.setStatus("PENDING");
paymentTransaction.setTransactionDate(LocalDateTime.now());
paymentTransactionRepository.save(paymentTransaction); // ✅ FIX
```

#### Test:
1. Đặt hàng với COD
2. Check database:
   ```sql
   SELECT * FROM payment_transactions;
   ```
3. Verify có record mới với status = "PENDING"

---

### ❌ 3. THÊM MISSING METHODS VÀO ORDERREPOSITORY

**File:** `d:\BoizShop\2BShop\src\main\java\boiz\shop\_2BShop\respository\OrderRepository.java`  
**Vấn đề:** AdminController cần các methods này nhưng chưa có  
**Ước tính:** 20 phút

#### Methods cần thêm:

```java
package boiz.shop._2BShop.respository;

import boiz.shop._2BShop.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    
    // ✅ Existing methods
    List<Order> findByUserOrderByOrderDateDesc(User user);
    List<Order> findByUserUserId(Integer userId);
    List<Order> findByOrderStatus(String status);
    
    // ❌ MISSING - Cần thêm:
    
    // 1. Tìm orders theo date range (cho dashboard filter)
    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    List<Order> findByOrderDateBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // 2. Tính tổng doanh thu theo date range và status
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate AND o.orderStatus IN :statuses")
    BigDecimal sumTotalAmountByDateRangeAndStatus(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("statuses") List<String> statuses
    );
    
    // 3. Lấy top 10 orders gần nhất (cho dashboard)
    List<Order> findTop10ByOrderByOrderDateDesc();
    
    // 4. Đếm orders theo status (cho dashboard)
    Long countByOrderStatus(String status);
    
    // 5. Tìm orders với pagination & filters (cho admin order management)
    Page<Order> findByOrderStatusAndOrderDateBetween(
        String status,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Pageable pageable
    );
    
    // 6. Search orders by receiver name or phone
    @Query("SELECT o FROM Order o WHERE o.receiverName LIKE %:keyword% OR o.shippingPhone LIKE %:keyword%")
    Page<Order> searchOrders(@Param("keyword") String keyword, Pageable pageable);
}
```

#### Import cần thêm:
```java
import java.math.BigDecimal;
import java.time.LocalDateTime;
```

---

## 🟡 HIGH PRIORITY

### ❌ 4. HOÀN THIỆN DASHBOARDSERVICE

**File:** `d:\BoizShop\2BShop\src\main\java\boiz\shop\_2BShop\service\DashboardService.java`  
**Trạng thái:** File bị comment hoặc chưa hoàn chỉnh  
**Ước tính:** 3 giờ

#### Methods cần implement:

```java
package boiz.shop._2BShop.service;

import boiz.shop._2BShop.entity.Order;
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
     * @param period: "today", "week", "month", "quarter", "year"
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
     */
    public Long getUserCount() {
        // Tất cả users trừ đi số admins
        long totalUsers = userRepository.count();
        // Giả sử có 1-2 admin accounts
        return totalUsers > 0 ? totalUsers - 1 : 0;
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
        Map<String, Long> stats = new HashMap<>();
        
        // Lấy tất cả order details
        List<OrderDetail> allDetails = orderDetailRepository.findAll();
        
        // Group by brand
        for (OrderDetail detail : allDetails) {
            String brandName = detail.getWatch().getBrand().getBrandName();
            stats.put(brandName, stats.getOrDefault(brandName, 0L) + 1);
        }
        
        return stats;
    }
    
    /**
     * Data cho biểu đồ doanh thu (Chart.js)
     * @param period: "week" (7 days), "month" (30 days), "year" (12 months)
     * @return Map với labels và data arrays
     */
    public Map<String, Object> getRevenueChartData(String period) {
        Map<String, Object> chartData = new HashMap<>();
        
        if ("year".equals(period)) {
            // 12 tháng gần nhất
            List<String> labels = Arrays.asList(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            );
            List<BigDecimal> data = new ArrayList<>();
            
            LocalDate now = LocalDate.now();
            for (int i = 11; i >= 0; i--) {
                LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
                LocalDate monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth());
                
                BigDecimal revenue = orderRepository.sumTotalAmountByDateRangeAndStatus(
                    monthStart.atStartOfDay(),
                    monthEnd.atTime(LocalTime.MAX),
                    Arrays.asList("DELIVERED", "COMPLETED")
                );
                
                data.add(revenue != null ? revenue : BigDecimal.ZERO);
            }
            
            chartData.put("labels", labels);
            chartData.put("data", data);
        }
        // Tương tự cho "week" và "month"...
        
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
            
            default:
                return today.atStartOfDay();
        }
    }
}
```

#### Additional Repository Method Needed:

**WatchRepository.java:**
```java
// Đếm số watches active
Long countByIsActiveTrue();
```

---

### ❌ 5. ADMIN DASHBOARD PAGE

**Route:** `/admin` hoặc `/admin/dashboard`  
**Template:** `d:\BoizShop\2BShop\src\main\resources\templates\admin\dashboard.html`  
**Ước tính:** 4 giờ

#### Features cần implement:

##### A. Statistics Cards (4 cards)
```html
<div class="stats-cards">
    <!-- Card 1: Total Revenue -->
    <div class="stat-card">
        <div class="stat-icon">💰</div>
        <div class="stat-info">
            <h3 th:text="${#numbers.formatDecimal(revenue, 0, 'COMMA', 0, 'POINT')} + '₫'">0₫</h3>
            <p>Tổng Doanh Thu</p>
        </div>
    </div>
    
    <!-- Card 2: Total Orders -->
    <div class="stat-card">
        <div class="stat-icon">📦</div>
        <div class="stat-info">
            <h3 th:text="${orderCount}">0</h3>
            <p>Tổng Đơn Hàng</p>
        </div>
    </div>
    
    <!-- Card 3: Total Products -->
    <div class="stat-card">
        <div class="stat-icon">⌚</div>
        <div class="stat-info">
            <h3 th:text="${productCount}">0</h3>
            <p>Tổng Sản Phẩm</p>
        </div>
    </div>
    
    <!-- Card 4: Total Users -->
    <div class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-info">
            <h3 th:text="${userCount}">0</h3>
            <p>Tổng Người Dùng</p>
        </div>
    </div>
</div>
```

##### B. Period Filter
```html
<div class="period-filter">
    <label>Lọc theo:</label>
    <select id="periodSelect" onchange="filterByPeriod(this.value)">
        <option value="today">Hôm nay</option>
        <option value="week">Tuần này</option>
        <option value="month" selected>Tháng này</option>
        <option value="quarter">Quý này</option>
        <option value="year">Năm này</option>
    </select>
</div>

<script>
function filterByPeriod(period) {
    window.location.href = '/admin/dashboard?period=' + period;
}
</script>
```

##### C. Recent Orders Table
```html
<div class="recent-orders">
    <h3>Đơn Hàng Gần Đây</h3>
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
                <td th:text="${#temporals.format(order.orderDate, 'dd/MM/yyyy HH:mm')}">11/01/2026 14:30</td>
                <td th:text="${#numbers.formatDecimal(order.totalAmount, 0, 'COMMA', 0, 'POINT')} + '₫'">1,000,000₫</td>
                <td>
                    <span class="badge" th:classappend="${order.orderStatus}" th:text="${order.orderStatus}">PENDING</span>
                </td>
                <td>
                    <a th:href="@{/admin/orders/{id}(id=${order.orderId})}" class="btn-view">Xem</a>
                </td>
            </tr>
        </tbody>
    </table>
</div>
```

##### D. Order Status Statistics
```html
<div class="order-stats">
    <h3>Thống Kê Đơn Hàng Theo Trạng Thái</h3>
    <div class="stats-grid">
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
</div>
```

##### E. Brand Statistics (Optional - Biểu đồ)
```html
<div class="brand-stats">
    <h3>Thống Kê Theo Thương Hiệu</h3>
    <canvas id="brandChart"></canvas>
</div>

<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script th:inline="javascript">
const brandData = /*[[${orderStatsByBrand}]]*/ {};

const ctx = document.getElementById('brandChart').getContext('2d');
new Chart(ctx, {
    type: 'pie',
    data: {
        labels: Object.keys(brandData),
        datasets: [{
            data: Object.values(brandData),
            backgroundColor: [
                '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF'
            ]
        }]
    }
});
</script>
```

#### AdminController Method:
```java
@GetMapping({"/", "/dashboard"})
public String dashboard(
    @RequestParam(defaultValue = "month") String period,
    Model model
) {
    model.addAttribute("revenue", dashboardService.getRevenue(period));
    model.addAttribute("orderCount", dashboardService.getOrderCount(period));
    model.addAttribute("productCount", dashboardService.getProductCount());
    model.addAttribute("userCount", dashboardService.getUserCount());
    model.addAttribute("recentOrders", dashboardService.getRecentOrders());
    model.addAttribute("orderStatsByStatus", dashboardService.getOrderStatsByStatus());
    model.addAttribute("orderStatsByBrand", dashboardService.getOrderStatsByBrand());
    model.addAttribute("selectedPeriod", period);
    
    return "admin/dashboard";
}
```

---

### ❌ 6. ADMIN WATCH MANAGEMENT

**Route:** `/admin/watches`  
**Template:** `templates/admin/watches.html`  
**Ước tính:** 6 giờ

#### A. Watch List Page

**Features:**
- [ ] Hiển thị tất cả watches với pagination (20 items/page)
- [ ] Search by name/brand
- [ ] Filter by:
  - Brand (dropdown)
  - Category (dropdown)
  - Stock status (Còn hàng, Hết hàng, Sắp hết)
  - Active status (Active, Inactive)
- [ ] Table columns:
  - Image thumbnail
  - Name
  - Brand
  - Category
  - Price
  - Discount %
  - Stock
  - Status
  - Actions (Edit, Delete)
- [ ] "Thêm sản phẩm mới" button

**Controller Method:**
```java
@GetMapping("/watches")
public String watchList(
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) String brand,
    @RequestParam(required = false) String category,
    @RequestParam(required = false) Boolean isActive,
    @RequestParam(defaultValue = "0") int page,
    Model model
) {
    Pageable pageable = PageRequest.of(page, 20);
    
    Page<Watch> watches;
    if (keyword != null || brand != null || category != null || isActive != null) {
        // Apply filters
        watches = watchService.searchWatchesAdmin(keyword, brand, category, isActive, pageable);
    } else {
        watches = watchService.getAllWatches(pageable);
    }
    
    model.addAttribute("watches", watches);
    model.addAttribute("brands", watchBrandRepository.findAll());
    model.addAttribute("categories", watchCategoryRepository.findAll());
    
    return "admin/watches";
}
```

---

#### B. Add/Edit Watch Form

**Route:** `/admin/watches/new`, `/admin/watches/edit/{id}`  
**Template:** `templates/admin/watch-form.html`

**Form Fields:**
- [ ] Watch Name (text, required)
- [ ] Brand (select dropdown, required)
- [ ] Category (select dropdown, required)
- [ ] Price (number, required, min=0)
- [ ] Discount Percent (number, optional, 0-100)
- [ ] Stock Quantity (number, required, min=0)
- [ ] Description (textarea, optional)
- [ ] Specifications (textarea, optional - JSON format)
- [ ] Main Image (file upload, required for new)
- [ ] Gallery Images (multiple file upload, optional)
- [ ] Active Status (checkbox)

**Controller Methods:**
```java
@GetMapping("/watches/new")
public String newWatchForm(Model model) {
    model.addAttribute("watch", new Watch());
    model.addAttribute("brands", watchBrandRepository.findAll());
    model.addAttribute("categories", watchCategoryRepository.findAll());
    model.addAttribute("isEdit", false);
    return "admin/watch-form";
}

@GetMapping("/watches/edit/{id}")
public String editWatchForm(@PathVariable Integer id, Model model) {
    Watch watch = watchService.getWatchById(id)
        .orElseThrow(() -> new RuntimeException("Watch not found"));
    
    model.addAttribute("watch", watch);
    model.addAttribute("brands", watchBrandRepository.findAll());
    model.addAttribute("categories", watchCategoryRepository.findAll());
    model.addAttribute("isEdit", true);
    
    return "admin/watch-form";
}

@PostMapping("/watches/save")
public String saveWatch(
    @ModelAttribute Watch watch,
    @RequestParam(required = false) MultipartFile mainImage,
    @RequestParam(required = false) List<MultipartFile> galleryImages,
    RedirectAttributes redirectAttributes
) {
    try {
        // Upload main image
        if (mainImage != null && !mainImage.isEmpty()) {
            String imagePath = fileUploadService.uploadWatchImage(mainImage, "main");
            // Set image path to watch...
        }
        
        // Upload gallery images
        if (galleryImages != null) {
            for (MultipartFile file : galleryImages) {
                if (!file.isEmpty()) {
                    String imagePath = fileUploadService.uploadWatchImage(file, "gallery");
                    // Save to watch_images table...
                }
            }
        }
        
        watchService.saveWatch(watch);
        redirectAttributes.addFlashAttribute("success", "Lưu sản phẩm thành công!");
        
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
    }
    
    return "redirect:/admin/watches";
}
```

---

#### C. Delete Watch

**Route:** `DELETE /admin/watches/delete/{id}`

**Logic:**
1. Check xem watch có trong orders chưa
2. Nếu có → Không cho xóa, chỉ cho inactive
3. Nếu không → Xóa watch + images

**Controller Method:**
```java
@PostMapping("/watches/delete/{id}")
public String deleteWatch(
    @PathVariable Integer id,
    RedirectAttributes redirectAttributes
) {
    try {
        // Check if watch is in any order
        boolean hasOrders = orderDetailRepository.existsByWatchWatchId(id);
        
        if (hasOrders) {
            // Không xóa, chỉ inactive
            Watch watch = watchService.getWatchById(id).orElseThrow();
            watch.setIsActive(false);
            watchRepository.save(watch);
            
            redirectAttributes.addFlashAttribute("warning", 
                "Sản phẩm đã có trong đơn hàng. Đã chuyển sang trạng thái Inactive.");
        } else {
            // Xóa images trước
            Watch watch = watchService.getWatchById(id).orElseThrow();
            for (WatchImage img : watch.getImages()) {
                fileUploadService.deleteWatchImage(img.getImageUrl());
            }
            
            // Xóa watch
            watchRepository.deleteById(id);
            
            redirectAttributes.addFlashAttribute("success", "Xóa sản phẩm thành công!");
        }
        
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
    }
    
    return "redirect:/admin/watches";
}
```

#### D. FileUploadService (Cần tạo mới)

**File:** `service/FileUploadService.java`

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
        // Create directory if not exists
        String dirPath = UPLOAD_DIR + "watches/" + subfolder + "/";
        Path directory = Paths.get(dirPath);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFilename = UUID.randomUUID().toString() + extension;
        
        // Save file
        Path filePath = directory.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative path
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
        
        String fullPath = UPLOAD_DIR + imagePath.replace("/uploads/", "");
        Path path = Paths.get(fullPath);
        
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }
}
```

**application.properties:**
```properties
# File upload settings
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=50MB
```

**Static Resource Mapping (WebConfig.java):**
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:d:/BoizShop/uploads/");
    }
}
```

---

### ❌ 7. ADMIN ORDER MANAGEMENT

**Route:** `/admin/orders`  
**Template:** `templates/admin/orders.html`  
**Ước tính:** 5 giờ

#### A. Order List Page

**Features:**
- [ ] List all orders với pagination (20 items/page)
- [ ] Filter by:
  - Status (dropdown: All, PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED)
  - Date range (from - to)
  - Payment method
- [ ] Search by:
  - Order ID
  - Customer name
  - Phone number
- [ ] Table columns:
  - Order ID
  - Customer name
  - Order date
  - Total amount
  - Payment method
  - Status badge
  - Actions (View, Update Status)
- [ ] Color-coded status badges

**Controller Method:**
```java
@GetMapping("/orders")
public String orderList(
    @RequestParam(required = false) String status,
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
    @RequestParam(defaultValue = "0") int page,
    Model model
) {
    Pageable pageable = PageRequest.of(page, 20, Sort.by("orderDate").descending());
    
    Page<Order> orders;
    
    if (keyword != null) {
        orders = orderRepository.searchOrders(keyword, pageable);
    } else if (status != null && fromDate != null && toDate != null) {
        orders = orderRepository.findByOrderStatusAndOrderDateBetween(
            status, 
            fromDate.atStartOfDay(), 
            toDate.atTime(LocalTime.MAX), 
            pageable
        );
    } else if (status != null) {
        orders = orderRepository.findByOrderStatus(status, pageable);
    } else {
        orders = orderRepository.findAll(pageable);
    }
    
    model.addAttribute("orders", orders);
    model.addAttribute("paymentMethods", paymentMethodRepository.findAll());
    
    return "admin/orders";
}
```

---

#### B. Order Detail Page

**Route:** `/admin/orders/{id}`

**Features:**
- [ ] Full order information:
  - Order ID, Date, Status
  - Customer info (name, email, phone)
  - Shipping address
  - Payment method
  - Notes
- [ ] Product list table:
  - Image, Name, Brand
  - Quantity, Unit price, Discount, Subtotal
- [ ] Summary:
  - Subtotal, Shipping fee, Total
- [ ] Order timeline (status history)
- [ ] Update status form:
  - Status dropdown (chỉ hiển thị status hợp lệ theo flow)
  - Confirm button
- [ ] Cancel button (nếu PENDING/CONFIRMED)
- [ ] Print invoice button (optional)

**Status Flow:**
```
PENDING → CONFIRMED → SHIPPING → DELIVERED
   ↓
CANCELLED (chỉ từ PENDING/CONFIRMED)
```

**Controller Method:**
```java
@GetMapping("/orders/{id}")
public String orderDetail(@PathVariable Integer id, Model model) {
    Order order = orderRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Order not found"));
    
    List<OrderDetail> orderDetails = orderDetailRepository.findByOrder(order);
    
    // Determine valid next statuses
    List<String> validStatuses = getValidNextStatuses(order.getOrderStatus());
    
    model.addAttribute("order", order);
    model.addAttribute("orderDetails", orderDetails);
    model.addAttribute("validStatuses", validStatuses);
    
    return "admin/order-detail";
}

private List<String> getValidNextStatuses(String currentStatus) {
    switch (currentStatus) {
        case "PENDING":
            return Arrays.asList("CONFIRMED", "CANCELLED");
        case "CONFIRMED":
            return Arrays.asList("SHIPPING", "CANCELLED");
        case "SHIPPING":
            return Arrays.asList("DELIVERED");
        default:
            return Collections.emptyList();
    }
}

@PostMapping("/orders/update-status")
public String updateOrderStatus(
    @RequestParam Integer orderId,
    @RequestParam String newStatus,
    RedirectAttributes redirectAttributes
) {
    try {
        orderService.updateOrderStatus(orderId, newStatus);
        redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
    }
    
    return "redirect:/admin/orders/" + orderId;
}
```

---

### ❌ 8. ADMIN USER MANAGEMENT

**Route:** `/admin/users`  
**Template:** `templates/admin/users.html`  
**Ước tính:** 3 giờ

#### Features:
- [ ] List all users (không hiển thị admin)
- [ ] Table columns:
  - User ID
  - Username
  - Email
  - Full Name
  - Phone
  - Join Date
  - Total Orders
  - Total Spent
  - Status (Active/Banned)
  - Actions (Ban/Unban, View Orders)
- [ ] Search by username/email/name
- [ ] Filter by:
  - Status (Active, Banned)
  - Join date range
- [ ] Ban/Unban functionality
- [ ] View user's order history

**Controller Methods:**
```java
@GetMapping("/users")
public String userList(
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) Boolean isActive,
    @RequestParam(defaultValue = "0") int page,
    Model model
) {
    Pageable pageable = PageRequest.of(page, 20);
    
    Page<User> users;
    if (keyword != null) {
        users = userRepository.searchUsers(keyword, pageable);
    } else if (isActive != null) {
        users = userRepository.findByIsActive(isActive, pageable);
    } else {
        users = userRepository.findAll(pageable);
    }
    
    model.addAttribute("users", users);
    return "admin/users";
}

@PostMapping("/users/ban/{id}")
public String banUser(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
    try {
        User user = userRepository.findById(id).orElseThrow();
        user.setIsActive(false);
        userRepository.save(user);
        
        redirectAttributes.addFlashAttribute("success", "Đã ban user thành công!");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
    }
    
    return "redirect:/admin/users";
}

@PostMapping("/users/unban/{id}")
public String unbanUser(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
    try {
        User user = userRepository.findById(id).orElseThrow();
        user.setIsActive(true);
        userRepository.save(user);
        
        redirectAttributes.addFlashAttribute("success", "Đã unban user thành công!");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
    }
    
    return "redirect:/admin/users";
}
```

**Additional UserRepository Methods:**
```java
@Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.email LIKE %:keyword% OR u.fullName LIKE %:keyword%")
Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

Page<User> findByIsActive(Boolean isActive, Pageable pageable);
```

---

## 🟢 MEDIUM PRIORITY

### ❌ 9. VNPAY INTEGRATION COMPLETION

**Trạng thái:** Code đã có, chưa đăng ký Sandbox  
**Ước tính:** 1 giờ (chờ VNPay approve)

#### Steps:
1. [ ] Đăng ký VNPay Sandbox tại: https://sandbox.vnpayment.vn/devreg/
2. [ ] Điền form:
   - Merchant Name: 2BShop
   - Return URL: http://your-domain/payment/vnpay-return
   - Email & Phone
3. [ ] Đợi email từ VNPay (5-10 phút)
4. [ ] Nhận được TMN Code & Hash Secret
5. [ ] Cập nhật `application.properties`:
   ```properties
   vnpay.tmnCode=YOUR_TMN_CODE_FROM_EMAIL
   vnpay.hashSecret=YOUR_HASH_SECRET_FROM_EMAIL
   ```
6. [ ] Test thanh toán với thẻ NCB test:
   - Số thẻ: 9704198526191432198
   - Tên: NGUYEN VAN A
   - Ngày: 07/15
   - OTP: 123456

#### Test Checklist:
- [ ] Redirect đến VNPay thành công
- [ ] Thanh toán thành công → Callback nhận được
- [ ] Order status update → CONFIRMED
- [ ] PaymentTransaction saved với status SUCCESS
- [ ] Redirect về payment-result.html với thông báo thành công

---

### ❌ 10. OAUTH2 LOGIN SETUP (OPTIONAL)

**Trạng thái:** Code đã có, chưa setup credentials  
**Guide:** `OAUTH2_LOGIN_GUIDE.md`  
**Ước tính:** 2 giờ

#### Google OAuth2:
1. [ ] Tạo project tại: https://console.cloud.google.com/
2. [ ] Create OAuth2 credentials
3. [ ] Authorized redirect URIs: `http://localhost:8080/login/oauth2/code/google`
4. [ ] Copy Client ID & Client Secret
5. [ ] Update `application.properties`:
   ```properties
   spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
   spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET
   ```

#### Facebook OAuth2:
1. [ ] Tạo app tại: https://developers.facebook.com/
2. [ ] Add Facebook Login product
3. [ ] Valid OAuth Redirect URIs: `http://localhost:8080/login/oauth2/code/facebook`
4. [ ] Copy App ID & App Secret
5. [ ] Update `application.properties`:
   ```properties
   spring.security.oauth2.client.registration.facebook.client-id=YOUR_APP_ID
   spring.security.oauth2.client.registration.facebook.client-secret=YOUR_APP_SECRET
   ```

#### Test:
- [ ] Click "Đăng nhập với Google" → Redirect to Google → Login → Redirect back → User created/logged in
- [ ] Click "Đăng nhập với Facebook" → Similar flow
- [ ] Check database: `Users` table có oauth2_provider và oauth2_provider_id

---

## ⚪ LOW PRIORITY (Optional Features)

### ❌ 11. EMAIL ORDER STATUS UPDATES

Send email khi admin update order status:
- CONFIRMED → "Đơn hàng đã được xác nhận"
- SHIPPING → "Đơn hàng đang được giao"
- DELIVERED → "Đơn hàng đã giao thành công"
- CANCELLED → "Đơn hàng đã bị hủy"

---

### ❌ 12. PRODUCT REVIEW SYSTEM

Allow users to review products after receiving order:
- Rating (1-5 stars)
- Comment
- Images (optional)
- Display reviews on product detail page

---

### ❌ 13. WISHLIST FEATURE

- Add to wishlist button
- Wishlist page
- Move from wishlist to cart

---

### ❌ 14. ADVANCED SEARCH & FILTERS

- Price slider
- Multiple brand selection (checkboxes)
- Sort by: Price (low-high, high-low), Newest, Best Sellers
- View mode: Grid/List

---

### ❌ 15. INVENTORY MANAGEMENT

- Stock alerts (low stock warning)
- Stock history (nhập hàng, xuất hàng)
- Auto-send email to admin when stock < 10

---

### ❌ 16. PROMOTION & COUPON SYSTEM

- Create discount coupons
- Apply coupon at checkout
- Automatic promotions (Flash sale, Buy 2 Get 1, etc.)

---

### ❌ 17. SHIPPING PROVIDER INTEGRATION

- GHN, Giao Hàng Tiết Kiệm API
- Auto-calculate shipping fee by address
- Real-time tracking

---

### ❌ 18. MULTI-LANGUAGE SUPPORT

- Vietnamese / English
- i18n configuration
- Language switcher

---

### ❌ 19. EXPORT REPORTS

- Export orders to Excel/CSV
- Export revenue reports
- Export product inventory

---

### ❌ 20. ADMIN ACTIVITY LOGS

- Log all admin actions (create, update, delete)
- Who did what and when
- Audit trail

---

## 📊 SUMMARY TIMELINE

| **Task** | **Priority** | **Estimate** | **Depends On** |
|----------|--------------|--------------|----------------|
| #1. Uncomment AdminController | 🔴 CRITICAL | 2h | #2, #3 |
| #2. Fix OrderService Bug | 🔴 CRITICAL | 15min | - |
| #3. Add OrderRepository Methods | 🔴 CRITICAL | 20min | - |
| #4. DashboardService | 🟡 HIGH | 3h | #3 |
| #5. Admin Dashboard Page | 🟡 HIGH | 4h | #1, #4 |
| #6. Admin Watch Management | 🟡 HIGH | 6h | #1, FileUploadService |
| #7. Admin Order Management | 🟡 HIGH | 5h | #1, #3 |
| #8. Admin User Management | 🟡 HIGH | 3h | #1 |
| #9. VNPay Completion | 🟢 MEDIUM | 1h | - |
| #10. OAuth2 Setup | 🟢 MEDIUM | 2h | - |

**Total Critical & High Priority:** ~23-25 giờ làm việc

---

## 🎯 RECOMMENDED EXECUTION ORDER

### Phase 1: Fix Critical Bugs (3-4 giờ)
1. ✅ Fix OrderService Bug (#2)
2. ✅ Add OrderRepository Methods (#3)
3. ✅ Uncomment & Fix AdminController (#1)

### Phase 2: Backend Logic (3 giờ)
4. ✅ Implement DashboardService (#4)
5. ✅ Create FileUploadService (for Watch Management)

### Phase 3: Admin UI (18 giờ)
6. ✅ Admin Dashboard (#5)
7. ✅ Admin Watch Management (#6)
8. ✅ Admin Order Management (#7)
9. ✅ Admin User Management (#8)

### Phase 4: External Integrations (3 giờ - Optional)
10. ✅ VNPay Completion (#9)
11. ✅ OAuth2 Setup (#10)

### Phase 5: Enhancements (Low Priority - Optional)
12. Features #11-#20 theo nhu cầu

---

## 📞 HỖ TRỢ KHI GẶP LỖI

### Common Issues:

#### 1. **AdminController không compile**
- Check tất cả dependencies đã có trong pom.xml
- Check tất cả services đã implement
- Check tất cả repositories có methods cần thiết

#### 2. **File upload không hoạt động**
- Check folder permissions (d:/BoizShop/uploads/)
- Check multipart config trong application.properties
- Check WebConfig addResourceHandlers

#### 3. **Database queries lỗi**
- Check entity relationships (@ManyToOne, @OneToMany)
- Check query syntax (JPQL vs native SQL)
- Check parameter names match

#### 4. **VNPay callback không nhận**
- Check ngrok đang chạy
- Check returnUrl trong application.properties khớp với VNPay config
- Check PaymentController endpoint chính xác

---

**🎊 HÃY BẮT ĐẦU VỚI PHASE 1 - FIX CRITICAL BUGS! 🎊**
