# 🏷️ SPRING BOOT ANNOTATIONS - 2BSHOP

> **Tài liệu này giải thích chi tiết tất cả Spring Boot Annotations được sử dụng trong dự án 2BShop**

---

## 📋 MỤC LỤC

1. [Layer Annotations (Phân tầng)](#1-layer-annotations-phân-tầng)
2. [Routing Annotations (Định tuyến)](#2-routing-annotations-định-tuyến)
3. [Dependency Injection (Tiêm phụ thuộc)](#3-dependency-injection-tiêm-phụ-thuộc)
4. [Data Binding (Liên kết dữ liệu)](#4-data-binding-liên-kết-dữ-liệu)
5. [Validation Annotations (Kiểm tra dữ liệu)](#5-validation-annotations-kiểm-tra-dữ-liệu)
6. [Transaction & Persistence (Giao dịch & Dữ liệu)](#6-transaction--persistence-giao-dịch--dữ-liệu)
7. [Security Annotations (Bảo mật)](#7-security-annotations-bảo-mật)
8. [Configuration Annotations (Cấu hình)](#8-configuration-annotations-cấu-hình)
9. [Response Handling (Xử lý phản hồi)](#9-response-handling-xử-lý-phản-hồi)
10. [JPA & Database Annotations](#10-jpa--database-annotations)

---

## 1. LAYER ANNOTATIONS (PHÂN TẦNG)

### 1.1. @Controller

**🎯 Dùng để làm gì:**  
Đánh dấu class là một **Controller** trong MVC pattern (xử lý HTTP requests và trả về View templates)

**⚙️ Chức năng:**
- Đăng ký class như một Spring Bean
- Cho phép xử lý HTTP requests (`GET`, `POST`, ...)
- Trả về **view name** (String) để render HTML template
- Hỗ trợ `@RequestMapping`, `@GetMapping`, `@PostMapping`, ...

**📝 Ý nghĩa trong dự án:**
- Sử dụng cho các controller trả về **HTML pages** (Server-Side Rendering)
- Các controller như `PublicController`, `UserController`, `CheckoutController` đều dùng `@Controller`
- Kết hợp với Thymeleaf để render dynamic HTML

**💡 Ví dụ:**
```java
@Controller
public class PublicController {
    
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("watches", watchService.getNewestWatches());
        return "public/index";  // ← Trả về view name, không phải JSON
    }
}
```

**🔄 Flow:**
```
Browser Request → @Controller → Method → Model + View Name → Thymeleaf → HTML Response
```

---

### 1.2. @RestController

**🎯 Dùng để làm gì:**  
Đánh dấu class là một **REST API Controller** (xử lý HTTP requests và trả về dữ liệu JSON/XML)

**⚙️ Chức năng:**
- Kết hợp của `@Controller` + `@ResponseBody`
- **Tất cả methods** tự động serialize response thành JSON
- Không cần return view name
- Dùng cho RESTful API endpoints

**📝 Ý nghĩa trong dự án:**
- Sử dụng cho **API endpoints** phục vụ AJAX requests
- Các controller như `BankAdminController` (API save bank), `OrderTrackingController` dùng `@RestController`
- Trả về JSON cho frontend JavaScript xử lý

**💡 Ví dụ:**
```java
@RestController
@RequestMapping("/api")
public class OrderTrackingController {
    
    @GetMapping("/orders/{id}/tracking")
    public Map<String, Object> getTracking(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "SHIPPING");
        response.put("currentLocation", "Hà Nội");
        return response;  // ← Tự động convert thành JSON
    }
}
```

**🔄 Response:**
```json
{
  "status": "SHIPPING",
  "currentLocation": "Hà Nội"
}
```

**🆚 So sánh @Controller vs @RestController:**

| Feature | @Controller | @RestController |
|---------|-------------|-----------------|
| Response type | HTML (view template) | JSON/XML (data) |
| Use case | Server-Side Rendering | RESTful API |
| Cần @ResponseBody? | Có (cho JSON response) | Không (tự động) |
| Ví dụ | `PublicController` | `OrderTrackingController` |

---

### 1.3. @Service

**🎯 Dùng để làm gì:**  
Đánh dấu class là một **Service Layer** (chứa business logic)

**⚙️ Chức năng:**
- Đăng ký class như một Spring Bean
- Thể hiện **Business Logic Layer** trong kiến trúc 3-layer
- Được inject vào Controller để xử lý logic (không viết logic trong Controller)

**📝 Ý nghĩa trong dự án:**
- Tách biệt logic nghiệp vụ khỏi Controller (Controller chỉ làm routing + validation)
- Service chứa logic: tính toán, validation, gọi repository, xử lý data
- Tất cả business logic như `calculateTotalAmount`, `checkStock`, `sendEmail` đều ở Service

**💡 Ví dụ:**
```java
@Service
public class CheckoutService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private MailService mailService;
    
    // Business logic: Tạo đơn hàng
    public Order placeOrder(CheckoutDTO checkoutDTO, User user) {
        // 1. Validate stock
        validateStock(checkoutDTO.getItems());
        
        // 2. Tính tổng tiền
        BigDecimal totalAmount = calculateTotalAmount(checkoutDTO.getItems());
        
        // 3. Tạo order
        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(totalAmount);
        
        // 4. Lưu DB
        orderRepository.save(order);
        
        // 5. Gửi email
        mailService.sendOrderConfirmationEmail(order);
        
        return order;
    }
}
```

**🔄 Flow:**
```
Controller → @Service (Business Logic) → Repository (Database) → Return Result → Controller
```

---

### 1.4. @Repository

**🎯 Dùng để làm gì:**  
Đánh dấu interface/class là một **Repository** (truy cập cơ sở dữ liệu)

**⚙️ Chức năng:**
- Đăng ký interface như một Spring Bean
- Kế thừa `JpaRepository<Entity, ID>` để có sẵn CRUD methods
- Tự động xử lý exception (convert SQLException → DataAccessException)
- Hỗ trợ custom query methods

**📝 Ý nghĩa trong dự án:**
- Tất cả database operations đều qua Repository
- Kế thừa `JpaRepository` để có sẵn `save()`, `findById()`, `findAll()`, `delete()`, ...
- Custom methods như `findByEmail()`, `findByOrderStatus()`, ...

**💡 Ví dụ:**
```java
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    // Spring tự động implement method này dựa vào tên
    Optional<User> findByEmail(String email);
    
    // Custom query
    @Query("SELECT u FROM User u WHERE u.fullName LIKE %:keyword% OR u.email LIKE %:keyword%")
    List<User> searchUsers(@Param("keyword") String keyword);
    
    // Built-in methods (có sẵn từ JpaRepository):
    // - save(User user)
    // - findById(Integer id)
    // - findAll()
    // - deleteById(Integer id)
    // - count()
}
```

**🔄 Layer Structure:**
```
Controller → Service → @Repository → Database
```

---

## 2. ROUTING ANNOTATIONS (ĐỊNH TUYẾN)

### 2.1. @RequestMapping

**🎯 Dùng để làm gì:**  
Định nghĩa **base path** cho controller hoặc map HTTP request đến method

**⚙️ Chức năng:**
- Dùng ở **class level**: Set base path cho tất cả methods trong controller
- Dùng ở **method level**: Map cụ thể URL + HTTP method
- Hỗ trợ nhiều HTTP methods: `GET`, `POST`, `PUT`, `DELETE`, ...

**📝 Ý nghĩa trong dự án:**
- Tổ chức routing theo modules (user, admin, public)
- Base path giúp tránh lặp code (không cần viết `/user` ở mỗi method)

**💡 Ví dụ:**
```java
@Controller
@RequestMapping("/user")  // ← Base path cho tất cả methods
public class UserController {
    
    @GetMapping("/profile")  // → Full path: /user/profile
    public String profile() {
        return "user/profile";
    }
    
    @GetMapping("/orders")  // → Full path: /user/orders
    public String orders() {
        return "user/my-orders";
    }
}
```

**💡 Ví dụ (Method-level với multiple HTTP methods):**
```java
@RequestMapping(value = "/login", method = {RequestMethod.GET, RequestMethod.POST})
public String login() {
    // Xử lý cả GET và POST
}
```

---

### 2.2. @GetMapping

**🎯 Dùng để làm gì:**  
Map HTTP **GET request** đến method (shortcut của `@RequestMapping(method = RequestMethod.GET)`)

**⚙️ Chức năng:**
- Xử lý **GET requests** (lấy dữ liệu, hiển thị trang)
- Idempotent: Gọi nhiều lần không thay đổi state
- Dùng cho: Hiển thị form, list data, detail page, ...

**📝 Ý nghĩa trong dự án:**
- Tất cả các trang hiển thị (index, products, profile, ...) đều dùng GET
- Không modify data → Safe for caching/bookmarking

**💡 Ví dụ:**
```java
@Controller
public class PublicController {
    
    @GetMapping("/watches")
    public String listWatches(Model model) {
        model.addAttribute("watches", watchService.findAll());
        return "public/products";  // Hiển thị danh sách
    }
    
    @GetMapping("/watches/{id}")
    public String watchDetail(@PathVariable Integer id, Model model) {
        model.addAttribute("watch", watchService.findById(id));
        return "public/product-detail";  // Hiển thị chi tiết
    }
}
```

---

### 2.3. @PostMapping

**🎯 Dùng để làm gì:**  
Map HTTP **POST request** đến method (shortcut của `@RequestMapping(method = RequestMethod.POST)`)

**⚙️ Chức năng:**
- Xử lý **POST requests** (submit form, tạo/cập nhật dữ liệu)
- Non-idempotent: Gọi nhiều lần → Tạo nhiều records
- Dùng cho: Đăng ký, login, checkout, update profile, ...

**📝 Ý nghĩa trong dự án:**
- Tất cả form submissions (register, login, checkout, ...) đều dùng POST
- POST → Process → Redirect (POST-Redirect-GET pattern để tránh duplicate submission)

**💡 Ví dụ:**
```java
@Controller
public class PublicController {
    
    @PostMapping("/register")
    public String register(@ModelAttribute RegisterDTO dto, RedirectAttributes redirectAttributes) {
        try {
            userService.registerUser(dto);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công!");
            return "redirect:/login";  // ← POST-Redirect-GET pattern
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }
}
```

**🔄 POST-Redirect-GET Pattern:**
```
1. User submit form → POST /register
2. Server process → Save DB
3. Redirect → GET /login
4. Browser follows redirect → GET /login
5. Show login page (refreshing won't re-submit form)
```

---

### 2.4. @PutMapping & @DeleteMapping

**🎯 Dùng để làm gì:**  
Map HTTP **PUT/DELETE requests** đến method (RESTful API)

**⚙️ Chức năng:**
- `@PutMapping`: Update toàn bộ resource
- `@DeleteMapping`: Xóa resource
- Thường dùng với `@RestController` cho API

**💡 Ví dụ:**
```java
@RestController
@RequestMapping("/api/users")
public class UserApiController {
    
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Integer id, @RequestBody User user) {
        return userService.updateUser(id, user);
    }
    
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
    }
}
```

---

## 3. DEPENDENCY INJECTION (TIÊM PHỤ THUỘC)

### 3.1. @Autowired

**🎯 Dùng để làm gì:**  
Tự động **inject dependencies** (Spring tự động tìm và gán bean phù hợp)

**⚙️ Chức năng:**
- Spring tìm bean matching type → Inject vào field/constructor/setter
- Không cần `new` object (Spring IoC Container quản lý)
- Giảm coupling, dễ test (có thể mock dependencies)

**📝 Ý nghĩa trong dự án:**
- Controller inject Service, Service inject Repository
- Tất cả dependencies đều được Spring quản lý
- Không có `new OrderService()` trong code → Tất cả qua `@Autowired`

**💡 Ví dụ (Field Injection):**
```java
@Controller
public class CheckoutController {
    
    @Autowired
    private CheckoutService checkoutService;  // Spring tự inject
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private UserService userService;
}
```

**💡 Ví dụ (Constructor Injection - Recommended):**
```java
@Service
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final MailService mailService;
    
    @Autowired
    public OrderService(OrderRepository orderRepository, MailService mailService) {
        this.orderRepository = orderRepository;
        this.mailService = mailService;
    }
}
```

**🆚 Constructor vs Field Injection:**

| Feature | Field Injection | Constructor Injection |
|---------|----------------|----------------------|
| Syntax | `@Autowired private Service service;` | `@Autowired public Controller(Service service)` |
| Immutability | Không (có thể null) | Có (`final` fields) |
| Testability | Khó (cần reflection) | Dễ (pass mock qua constructor) |
| Recommended | ❌ (legacy) | ✅ (best practice) |

---

### 3.2. @Qualifier

**🎯 Dùng để làm gì:**  
Chỉ định **bean cụ thể** khi có nhiều beans cùng type

**⚙️ Chức năng:**
- Khi có 2+ implementations của cùng interface → Chỉ định bean nào cần inject
- Dùng kèm `@Autowired`

**💡 Ví dụ:**
```java
public interface PaymentService {
    void processPayment(Order order);
}

@Service("vnpayService")
public class VNPayService implements PaymentService { ... }

@Service("codService")
public class CODService implements PaymentService { ... }

// Sử dụng:
@Controller
public class PaymentController {
    
    @Autowired
    @Qualifier("vnpayService")  // ← Chỉ định bean cụ thể
    private PaymentService paymentService;
}
```

---

## 4. DATA BINDING (LIÊN KẾT DỮ LIỆU)

### 4.1. @RequestParam

**🎯 Dùng để làm gì:**  
Lấy **query parameters** từ URL (sau dấu `?`)

**⚙️ Chức năng:**
- Bind URL query params vào method parameters
- Hỗ trợ `required`, `defaultValue`
- Dùng cho: Filter, search, pagination, ...

**📝 Ý nghĩa trong dự án:**
- Tất cả search/filter đều dùng `@RequestParam`
- Pagination: `?page=0&size=10`
- Filters: `?status=PENDING&search=nguyen`

**💡 Ví dụ:**
```java
@Controller
public class UserController {
    
    @GetMapping("/orders")
    public String myOrders(
        @RequestParam(required = false) String status,         // Optional
        @RequestParam(defaultValue = "0") int page,            // Default value
        @RequestParam(defaultValue = "10") int size,
        Model model
    ) {
        // URL: /user/orders?status=PENDING&page=0&size=10
        Page<Order> orders = orderService.findOrders(status, page, size);
        model.addAttribute("orders", orders);
        return "user/my-orders";
    }
}
```

**🔍 URL Examples:**
```
/user/orders                        → status=null, page=0, size=10
/user/orders?status=PENDING         → status="PENDING", page=0, size=10
/user/orders?page=2&size=20         → status=null, page=2, size=20
```

---

### 4.2. @PathVariable

**🎯 Dùng để làm gì:**  
Lấy **biến từ URL path** (trong dấu `{}`)

**⚙️ Chức năng:**
- Extract path variables từ URL
- Dùng cho RESTful URLs: `/users/{id}`, `/orders/{orderId}`
- Type conversion tự động (String → Integer, ...)

**📝 Ý nghĩa trong dự án:**
- Tất cả detail pages dùng `@PathVariable`
- RESTful URL structure: `/admin/users/5/edit`, `/watches/10`

**💡 Ví dụ:**
```java
@Controller
public class PublicController {
    
    @GetMapping("/watches/{id}")
    public String watchDetail(@PathVariable Integer id, Model model) {
        // URL: /watches/5 → id = 5
        Watch watch = watchService.findById(id);
        model.addAttribute("watch", watch);
        return "public/product-detail";
    }
}
```

**💡 Ví dụ (Multiple path variables):**
```java
@GetMapping("/admin/users/{userId}/orders/{orderId}")
public String userOrderDetail(
    @PathVariable Integer userId,
    @PathVariable Integer orderId,
    Model model
) {
    // URL: /admin/users/5/orders/10 → userId=5, orderId=10
}
```

---

### 4.3. @ModelAttribute

**🎯 Dùng để làm gì:**  
Bind **form data** vào một object (DTO/Entity)

**⚙️ Chức năng:**
- Auto-bind tất cả form fields vào object properties
- Dùng cho form submission (register, checkout, update profile, ...)
- Thymeleaf binding: `th:object="${user}" th:field="*{email}"`

**📝 Ý nghĩa trong dự án:**
- Tất cả forms (register, login, checkout, ...) đều bind vào DTO
- Giảm code (không cần `@RequestParam` cho từng field)

**💡 Ví dụ:**
```java
@Controller
public class PublicController {
    
    @PostMapping("/register")
    public String register(@ModelAttribute RegisterDTO dto) {
        // Form fields tự động bind vào dto
        // No need @RequestParam username, @RequestParam email, ...
        
        userService.registerUser(dto);
        return "redirect:/login";
    }
}
```

**🎨 Thymeleaf Form:**
```html
<form th:action="@{/register}" th:object="${registerDTO}" method="post">
    <input type="text" th:field="*{username}" placeholder="Username" />
    <input type="email" th:field="*{email}" placeholder="Email" />
    <input type="password" th:field="*{password}" placeholder="Password" />
    <button type="submit">Đăng ký</button>
</form>
```

**🔄 Flow:**
```
Form (username=john, email=john@mail.com, password=123)
  ↓
POST /register
  ↓
@ModelAttribute RegisterDTO dto {
    username: "john",
    email: "john@mail.com",
    password: "123"
}
```

---

### 4.4. @RequestBody

**🎯 Dùng để làm gì:**  
Bind **JSON request body** vào object (cho REST API)

**⚙️ Chức năng:**
- Deserialize JSON → Java object
- Dùng với `@RestController` + AJAX requests
- Content-Type: `application/json`

**📝 Ý nghĩa trong dự án:**
- AJAX requests gửi JSON → Backend parse vào DTO
- Dùng trong Bank Account API, Order Tracking API

**💡 Ví dụ:**
```java
@RestController
@RequestMapping("/admin/bank-accounts/api")
public class BankAdminController {
    
    @PostMapping("/save")
    public Map<String, Object> saveBankAccount(@RequestBody BankAccount bankAccount) {
        // JSON request body tự động parse vào bankAccount object
        bankAccountService.save(bankAccount);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return response;
    }
}
```

**📤 AJAX Request:**
```javascript
fetch('/admin/bank-accounts/api/save', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
        bankName: 'Vietcombank',
        accountNumber: '1234567890',
        accountHolder: 'NGUYEN VAN A'
    })
});
```

---

### 4.5. @ResponseBody

**🎯 Dùng để làm gì:**  
Trả về **data thay vì view name** (serialize object → JSON/XML)

**⚙️ Chức năng:**
- Convert return object → JSON
- Không cần với `@RestController` (đã bao gồm sẵn)
- Dùng khi muốn return JSON từ `@Controller`

**💡 Ví dụ:**
```java
@Controller
public class OrderController {
    
    @GetMapping("/api/orders/{id}")
    @ResponseBody  // ← Trả về JSON thay vì view
    public Order getOrder(@PathVariable Integer id) {
        return orderService.findById(id);  // Serialize thành JSON
    }
}
```

**📥 Response:**
```json
{
  "orderId": 5,
  "orderDate": "2024-01-15",
  "totalAmount": 15000000,
  "orderStatus": "PENDING"
}
```

---

## 5. VALIDATION ANNOTATIONS (KIỂM TRA DỮ LIỆU)

### 5.1. @Valid

**🎯 Dùng để làm gì:**  
Kích hoạt **validation** cho object (kiểm tra constraints trong class)

**⚙️ Chức năng:**
- Validate object theo constraints (`@NotNull`, `@Email`, `@Size`, ...)
- Dùng với `@ModelAttribute` hoặc `@RequestBody`
- Nếu fail → `BindingResult` chứa errors

**📝 Ý nghĩa trong dự án:**
- Validate form data trước khi save
- Client-side + Server-side validation

**💡 Ví dụ (DTO with constraints):**
```java
public class RegisterDTO {
    
    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 20, message = "Username phải từ 3-20 ký tự")
    private String username;
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;
    
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu tối thiểu 6 ký tự")
    private String password;
}
```

**💡 Ví dụ (Controller validation):**
```java
@Controller
public class PublicController {
    
    @PostMapping("/register")
    public String register(
        @Valid @ModelAttribute RegisterDTO dto,  // ← Validate theo constraints
        BindingResult bindingResult,             // ← Chứa validation errors
        Model model
    ) {
        if (bindingResult.hasErrors()) {
            // Có lỗi validation → Hiển thị lại form với error messages
            return "public/register";
        }
        
        // Validation pass → Tiếp tục xử lý
        userService.registerUser(dto);
        return "redirect:/login";
    }
}
```

**🎨 Thymeleaf Error Display:**
```html
<form th:action="@{/register}" th:object="${registerDTO}" method="post">
    <input type="text" th:field="*{username}" />
    <span th:if="${#fields.hasErrors('username')}" th:errors="*{username}" class="error"></span>
    
    <button type="submit">Đăng ký</button>
</form>
```

---

### 5.2. Common Validation Constraints

**📋 Các annotation validation phổ biến:**

| Annotation | Mục đích | Ví dụ |
|------------|----------|-------|
| `@NotNull` | Field không được `null` | `@NotNull private Integer age;` |
| `@NotBlank` | String không blank (trim → not empty) | `@NotBlank private String name;` |
| `@NotEmpty` | Collection/Array không empty | `@NotEmpty private List<String> items;` |
| `@Email` | Validate email format | `@Email private String email;` |
| `@Size(min, max)` | Độ dài String hoặc size Collection | `@Size(min=3, max=20) private String username;` |
| `@Min(value)` | Số >= value | `@Min(0) private int quantity;` |
| `@Max(value)` | Số <= value | `@Max(100) private int discountPercent;` |
| `@Pattern(regex)` | Match regex pattern | `@Pattern(regexp="\\d{10}") private String phone;` |
| `@Past` | Date phải trong quá khứ | `@Past private LocalDate birthDate;` |
| `@Future` | Date phải trong tương lai | `@Future private LocalDate expiryDate;` |

---

## 6. TRANSACTION & PERSISTENCE (GIAO DỊCH & DỮ LIỆU)

### 6.1. @Transactional

**🎯 Dùng để làm gì:**  
Đánh dấu method/class chạy trong **database transaction**

**⚙️ Chức năng:**
- Tất cả DB operations trong method chạy trong 1 transaction
- **COMMIT** nếu method success
- **ROLLBACK** nếu có exception
- Đảm bảo data consistency (ACID properties)

**📝 Ý nghĩa trong dự án:**
- Các operations phức tạp (place order, cancel order, ...) cần transaction
- Nếu 1 step fail → Rollback tất cả (không để data inconsistent)

**💡 Ví dụ:**
```java
@Service
public class CheckoutService {
    
    @Transactional  // ← Toàn bộ method chạy trong 1 transaction
    public Order placeOrder(CheckoutDTO dto, User user) {
        // Step 1: Create order
        Order order = new Order();
        orderRepository.save(order);
        
        // Step 2: Create order details
        for (CartItem item : dto.getItems()) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            orderDetailRepository.save(detail);
        }
        
        // Step 3: Update stock
        for (CartItem item : dto.getItems()) {
            Watch watch = watchRepository.findById(item.getWatchId()).orElseThrow();
            watch.setStockQuantity(watch.getStockQuantity() - item.getQuantity());
            watchRepository.save(watch);
        }
        
        // Step 4: Clear cart
        cartService.clearCart(user);
        
        // Nếu bất kỳ step nào fail → ROLLBACK tất cả
        return order;
    }
}
```

**🔄 Transaction Flow:**
```
BEGIN TRANSACTION
  ↓
Save Order (step 1)
  ↓
Save OrderDetails (step 2)
  ↓
Update Stock (step 3)
  ↓
Exception thrown? → ROLLBACK (undo all changes)
  ↓
Success? → COMMIT (apply all changes)
```

**⚙️ Rollback Rules:**
```java
@Transactional(rollbackFor = Exception.class)  // Rollback cho mọi Exception
@Transactional(noRollbackFor = IllegalArgumentException.class)  // Không rollback cho Exception này
```

---

## 7. SECURITY ANNOTATIONS (BẢO MẬT)

### 7.1. @PreAuthorize

**🎯 Dùng để làm gì:**  
Kiểm tra **quyền truy cập** trước khi execute method

**⚙️ Chức năng:**
- Check permissions/roles trước khi chạy method
- Dùng SpEL expression
- Nếu không đủ quyền → `AccessDeniedException`

**💡 Ví dụ:**
```java
@Controller
@RequestMapping("/admin")
public class DashboardController {
    
    @PreAuthorize("hasRole('ADMIN')")  // ← Chỉ ADMIN mới truy cập được
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("revenue", dashboardService.getRevenue());
        return "admin/dashboard";
    }
}
```

**💡 Ví dụ (Complex expressions):**
```java
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")  // OR condition
@PreAuthorize("hasRole('ADMIN') and #userId == principal.userId")  // AND + parameter check
@PreAuthorize("isAuthenticated()")  // Login required
@PreAuthorize("permitAll()")  // Public access
```

---

### 7.2. @Secured

**🎯 Dùng để làm gì:**  
Giống `@PreAuthorize` nhưng đơn giản hơn (chỉ check roles)

**💡 Ví dụ:**
```java
@Secured("ROLE_ADMIN")  // Chỉ role ADMIN
@GetMapping("/admin/users")
public String listUsers() {
    // ...
}

@Secured({"ROLE_ADMIN", "ROLE_MANAGER"})  // Multiple roles
@GetMapping("/reports")
public String reports() {
    // ...
}
```

---

## 8. CONFIGURATION ANNOTATIONS (CẤU HÌNH)

### 8.1. @Configuration

**🎯 Dùng để làm gì:**  
Đánh dấu class là **configuration class** (chứa bean definitions)

**⚙️ Chức năng:**
- Thay thế XML configuration
- Chứa các `@Bean` methods
- Tự động scan và register beans

**💡 Ví dụ:**
```java
@Configuration
public class AppConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();  // Tạo bean RestTemplate
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

### 8.2. @Bean

**🎯 Dùng để làm gì:**  
Đăng ký method return value như một **Spring Bean**

**⚙️ Chức năng:**
- Method return value → Spring Bean (managed by IoC container)
- Dùng trong `@Configuration` class
- Bean có thể inject vào các class khác

**💡 Ví dụ:**
```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**").authenticated()
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/")
            );
        return http.build();
    }
}
```

---

### 8.3. @Component

**🎯 Dùng để làm gì:**  
Đánh dấu class là một **generic Spring Bean**

**⚙️ Chức năng:**
- Generic stereotype annotation
- `@Service`, `@Repository`, `@Controller` đều là specialized `@Component`
- Dùng khi class không thuộc layer cụ thể nào

**💡 Ví dụ:**
```java
@Component
public class JwtTokenUtil {
    
    public String generateToken(String username) {
        // Generate JWT token
    }
    
    public boolean validateToken(String token) {
        // Validate token
    }
}
```

---

## 9. RESPONSE HANDLING (XỬ LÝ PHẢN HỒI)

### 9.1. @ResponseStatus

**🎯 Dùng để làm gì:**  
Đặt **HTTP status code** cho response

**⚙️ Chức năng:**
- Set custom HTTP status (200, 201, 404, 500, ...)
- Dùng với `@ExceptionHandler` hoặc exception class

**💡 Ví dụ:**
```java
@ResponseStatus(HttpStatus.NOT_FOUND)  // 404
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

---

### 9.2. @ExceptionHandler

**🎯 Dùng để làm gì:**  
Xử lý **exceptions** trong controller

**⚙️ Chức năng:**
- Catch specific exception trong controller
- Return error view hoặc JSON response
- Centralized error handling

**💡 Ví dụ:**
```java
@Controller
public class PublicController {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";  // Render error page
    }
}
```

---

### 9.3. @ControllerAdvice

**🎯 Dùng để làm gì:**  
**Global exception handling** cho tất cả controllers

**⚙️ Chức năng:**
- Xử lý exceptions từ tất cả controllers
- Không cần lặp code `@ExceptionHandler` ở mỗi controller
- Có thể add global model attributes

**💡 Ví dụ:**
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(ResourceNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericError(Exception ex, Model model) {
        model.addAttribute("errorMessage", "An unexpected error occurred");
        return "error/500";
    }
}
```

---

## 10. JPA & DATABASE ANNOTATIONS

### 10.1. @Entity

**🎯 Dùng để làm gì:**  
Đánh dấu class là một **JPA Entity** (map vào database table)

**⚙️ Chức năng:**
- Class → Database table
- Fields → Table columns
- Managed by JPA EntityManager

**💡 Ví dụ:**
```java
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
}
```

---

### 10.2. @Id & @GeneratedValue

**🎯 Dùng để làm gì:**  
- `@Id`: Đánh dấu field là **Primary Key**
- `@GeneratedValue`: Auto-generate giá trị (auto-increment)

**💡 Ví dụ:**
```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)  // AUTO_INCREMENT in MySQL
private Integer userId;
```

---

### 10.3. @Column

**🎯 Dùng để làm gì:**  
Đặt **constraints** cho database column

**💡 Ví dụ:**
```java
@Column(name = "full_name", nullable = false, length = 100)
private String fullName;

@Column(unique = true)
private String email;
```

---

### 10.4. @OneToMany & @ManyToOne

**🎯 Dùng để làm gì:**  
Define **relationships** giữa entities

**💡 Ví dụ:**
```java
@Entity
public class Order {
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;  // 1 Order → Many OrderDetails
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;  // Many Orders → 1 User
}
```

---

### 10.5. @Query

**🎯 Dùng để làm gì:**  
Viết **custom JPQL/SQL query**

**💡 Ví dụ:**
```java
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    @Query("SELECT u FROM User u WHERE u.fullName LIKE %:keyword% OR u.email LIKE %:keyword%")
    List<User> searchUsers(@Param("keyword") String keyword);
}
```

---

## 📊 TỔNG KẾT ANNOTATIONS THEO CHỨC NĂNG

| Loại | Annotations |
|------|-------------|
| **Layer** | `@Controller`, `@RestController`, `@Service`, `@Repository`, `@Component` |
| **Routing** | `@RequestMapping`, `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping` |
| **DI** | `@Autowired`, `@Qualifier` |
| **Data Binding** | `@RequestParam`, `@PathVariable`, `@ModelAttribute`, `@RequestBody`, `@ResponseBody` |
| **Validation** | `@Valid`, `@NotNull`, `@NotBlank`, `@Email`, `@Size`, `@Min`, `@Max` |
| **Transaction** | `@Transactional` |
| **Security** | `@PreAuthorize`, `@Secured` |
| **Config** | `@Configuration`, `@Bean`, `@Component` |
| **Exception** | `@ExceptionHandler`, `@ControllerAdvice`, `@ResponseStatus` |
| **JPA** | `@Entity`, `@Id`, `@GeneratedValue`, `@Column`, `@OneToMany`, `@ManyToOne`, `@Query` |

---

## 🔄 ANNOTATION COMBINATION PATTERNS

### Pattern 1: MVC Controller
```java
@Controller
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        // ...
    }
}
```

### Pattern 2: REST API Controller
```java
@RestController
@RequestMapping("/api")
public class ApiController {
    
    @Autowired
    private OrderService orderService;
    
    @GetMapping("/orders/{id}")
    public Order getOrder(@PathVariable Integer id) {
        return orderService.findById(id);
    }
}
```

### Pattern 3: Service with Transaction
```java
@Service
public class CheckoutService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Transactional
    public Order placeOrder(CheckoutDTO dto) {
        // Business logic with transaction
    }
}
```

### Pattern 4: Form Validation
```java
@PostMapping("/register")
public String register(
    @Valid @ModelAttribute RegisterDTO dto,
    BindingResult bindingResult
) {
    if (bindingResult.hasErrors()) {
        return "register";
    }
    // Process...
}
```

---

**📝 Ghi chú cuối:** Tài liệu này giải thích tất cả annotations được sử dụng trong 2BShop. Để hiểu chức năng cụ thể, tham khảo `USER_FEATURES.md` và `ADMIN_FEATURES.md`.
