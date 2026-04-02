# MVC -> REST Full Conversion Guide (Khong doi logic)

## 1. Pham vi va nguyen tac

Muc tieu cua file nay:
- Chuyen toan bo controller MVC hien tai sang REST endpoint cho React.
- Format ro rang: code cu -> code moi.
- Chi doi kieu giao tiep HTTP (view/form -> JSON API), KHONG doi business logic service/repository.

Nguyen tac bat buoc:
- Service call giu nguyen y nguyen (ten ham, tham so, thu tu xu ly).
- Validation giu nguyen.
- Rule quyen truy cap giu nguyen.
- Neu API moi can bo sung thi chi la vo wrapper REST, khong doi rule nghiep vu.

## 2. Danh sach file MVC -> REST

MVC cu:
- controller/PublicController.java
- controller/PasswordResetController.java
- controller/CartController.java
- controller/UserController.java
- controller/CheckoutController.java
- controller/OrderTrackingController.java
- controller/UserProfileController.java
- controller/admin/DashboardController.java
- controller/admin/WatchAdminController.java
- controller/admin/OrderAdminController.java
- controller/admin/UserAdminController.java
- controller/admin/BrandAdminController.java
- controller/admin/BankAdminController.java
- controller/admin/PaymentAdminController.java
- controller/AccountController.java
- controller/InvoiceController.java
- controller/PaymentController.java

REST moi (tao/sua):
- controller/api/ProductApiController.java
- controller/api/AuthApiController.java
- controller/api/CartApiController.java
- controller/api/CheckoutApiController.java
- controller/api/OrderApiController.java
- controller/api/ProfileApiController.java
- controller/api/admin/DashboardAdminApiController.java
- controller/api/admin/WatchAdminApiController.java
- controller/api/admin/OrderAdminApiController.java
- controller/api/admin/UserAdminApiController.java
- controller/api/admin/BrandAdminApiController.java
- controller/api/admin/BankAdminApiController.java
- controller/api/admin/PaymentAdminApiController.java
- controller/api/InvoiceApiController.java (tao moi)
- controller/api/PaymentApiController.java (tao moi)
- controller/api/AccountApiController.java (tao moi)

## 3. Mapping chi tiet: bo o file cu -> them o file moi

## 3.1 PublicController -> ProductApiController + AuthApiController

Bo o file cu (sau khi FE doi endpoint):
- @GetMapping("/watches")
- @GetMapping("/watches/newest")
- @GetMapping("/watches/discount")
- @GetMapping("/watches/{id}")
- @PostMapping("/register")
- @PostMapping("/confirm-register")
- @PostMapping("/resend-verification")

Giu tam o file cu (view):
- @GetMapping("/")
- @GetMapping("/login")
- @GetMapping("/register")
- @GetMapping("/about") /contact /policy /terms /faq

Them o file moi:
- ProductApiController
  - GET /api/v1/products
  - GET /api/v1/products/newest
  - GET /api/v1/products/discount
  - GET /api/v1/products/{id}
- AuthApiController
  - POST /api/v1/auth/register
  - POST /api/v1/auth/confirm-register
  - POST /api/v1/auth/resend-verification

Code cu (MVC):
```java
@PostMapping("/register")
public String processRegister(@ModelAttribute("registerDTO") RegisterDTO dto,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
    try {
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email không được để trống!");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new RuntimeException("Mật khẩu phải có ít nhất 6 ký tự!");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp!");
        }
        if (userService.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }
        userService.registerUser(dto);
        redirectAttributes.addFlashAttribute("success", "Đăng ký thành công!");
        return "redirect:/login";
    } catch (Exception e) {
        model.addAttribute("error", e.getMessage());
        model.addAttribute("registerDTO", dto);
        return "public/register";
    }
}
```

Code moi (REST, logic giu nguyen):
```java
@PostMapping("/register")
public ResponseEntity<Map<String, Object>> register(@RequestBody RegisterDTO dto) {
    Map<String, Object> res = new HashMap<>();
    try {
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email không được để trống!");
        }
        if (dto.getPassword() == null || dto.getPassword().length() < 6) {
            throw new RuntimeException("Mật khẩu phải có ít nhất 6 ký tự!");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp!");
        }
        if (userService.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng!");
        }
        userService.registerUser(dto);
        res.put("success", true);
        res.put("message", "Đăng ký thành công!");
        return ResponseEntity.ok(res);
    } catch (Exception e) {
        res.put("success", false);
        res.put("message", e.getMessage());
        return ResponseEntity.badRequest().body(res);
    }
}
```

## 3.2 PasswordResetController -> AuthApiController

Bo o file cu:
- @PostMapping("/forgot-password")
- @PostMapping("/reset-password")

Them o file moi:
- POST /api/v1/auth/forgot-password
- POST /api/v1/auth/reset-password

Code cu:
```java
@PostMapping("/forgot-password")
public String processForgotPassword(@RequestParam("email") String email,
                                    RedirectAttributes redirectAttributes) {
    try {
        passwordResetService.createResetToken(email);
        redirectAttributes.addFlashAttribute("success", "Nếu email tồn tại...");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra. Vui lòng thử lại.");
    }
    return "redirect:/forgot-password";
}
```

Code moi:
```java
@PostMapping("/forgot-password")
public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> body) {
    try {
        passwordResetService.createResetToken(body.get("email"));
        return ResponseEntity.ok(Map.of("success", true, "message", "Nếu email tồn tại..."));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Có lỗi xảy ra. Vui lòng thử lại."));
    }
}
```

## 3.3 CartController + UserController(cart) -> CartApiController

Bo o file cu:
- UserController: /user/cart/add, /user/cart/update, /user/cart/remove (neu co)
- CartController: /cart/add, /cart/update, /cart/remove, /cart/select, /cart/select-all, /cart/count

Them o file moi:
- GET /api/v1/cart
- GET /api/v1/cart/count
- POST /api/v1/cart/add
- POST /api/v1/cart/update
- POST /api/v1/cart/remove
- POST /api/v1/cart/select
- POST /api/v1/cart/select-all

Code cu:
```java
@PostMapping("/cart/remove")
@ResponseBody
public ResponseEntity<?> removeCartItem(@RequestBody Map<String, Integer> request) {
    try {
        Integer cartItemId = request.get("cartItemId");
        if (cartItemId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu ID sản phẩm"));
        }
        cartService.removeCartItem(cartItemId);
        return ResponseEntity.ok().body(Map.of("success", true));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
    }
}
```

Code moi (giu nguyen logic):
```java
@PostMapping("/remove")
public ResponseEntity<?> remove(@RequestBody Map<String, Integer> request) {
    try {
        Integer cartItemId = request.get("cartItemId");
        if (cartItemId == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Thiếu ID sản phẩm"));
        }
        cartService.removeCartItem(cartItemId);
        return ResponseEntity.ok(Map.of("success", true));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
    }
}
```

## 3.4 CheckoutController + UserController(checkout) -> CheckoutApiController

Bo o file cu:
- @PostMapping("/checkout/place-order") (CheckoutController)
- @PostMapping("/user/checkout/process") (UserController)

Them o file moi:
- GET /api/v1/checkout/summary
- POST /api/v1/checkout/place-order

Code cu (CheckoutController):
```java
@PostMapping("/place-order")
public String placeOrder(@RequestParam String receiverName,
                         @RequestParam String phone,
                         @RequestParam String address,
                         @RequestParam String paymentMethod,
                         @RequestParam(required = false) String notes,
                         @RequestParam(required = false) String couponCode,
                         @RequestParam(required = false) Integer bankAccountId,
                         RedirectAttributes redirectAttributes) {
    try {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Order order = checkoutService.placeOrder(user, receiverName, phone, address, notes, paymentMethod, couponCode, bankAccountId);
        redirectAttributes.addFlashAttribute("success", "Đặt hàng thành công!");
        return "redirect:/user/orders";
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Lỗi đặt hàng: " + e.getMessage());
        return "redirect:/checkout";
    }
}
```

Code moi (giu logic service y nguyen):
```java
@PostMapping("/place-order")
public ResponseEntity<Map<String, Object>> placeOrder(@RequestBody Map<String, Object> body) {
    try {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        Order order = checkoutService.placeOrder(
                user,
                body.get("receiverName").toString(),
                body.get("phone").toString(),
                body.get("address").toString(),
                (String) body.get("notes"),
                body.get("paymentMethod").toString(),
                (String) body.get("couponCode"),
                body.get("bankAccountId") == null ? null : Integer.valueOf(body.get("bankAccountId").toString())
        );

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đặt hàng thành công",
                "orderId", order.getOrderId()
        ));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Lỗi đặt hàng: " + e.getMessage()));
    }
}
```

## 3.5 OrderTrackingController + UserController(orders) -> OrderApiController

Bo o file cu:
- GET /my-orders
- GET /my-orders/{orderId}
- GET /user/orders
- GET /user/orders/{id}
- POST /user/orders/{id}/cancel

Them o file moi:
- GET /api/v1/orders/my
- GET /api/v1/orders/{id}
- POST /api/v1/orders/{id}/cancel

Code cu:
```java
@PostMapping("/orders/{id}/cancel")
public String cancelOrder(@PathVariable Integer id,
                          @RequestParam(required = false) String reason,
                          Principal principal,
                          RedirectAttributes redirectAttributes) {
    try {
        Order order = orderService.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        String email = principal.getName();
        if (!order.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Bạn không có quyền hủy đơn hàng này");
        }
        if (!"PENDING".equals(order.getOrderStatus()) && !"CONFIRMED".equals(order.getOrderStatus())) {
            throw new RuntimeException("Không thể hủy đơn hàng ở trạng thái hiện tại");
        }
        orderService.cancelOrder(id, reason);
        redirectAttributes.addFlashAttribute("success", "Hủy đơn hàng thành công!");
        return "redirect:/user/orders/" + id;
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        return "redirect:/user/orders/" + id;
    }
}
```

Code moi:
```java
@PostMapping("/{id}/cancel")
public ResponseEntity<Map<String, Object>> cancel(@PathVariable Integer id,
                                                  @RequestBody(required = false) Map<String, Object> body,
                                                  Principal principal) {
    try {
        Order order = orderService.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        String email = principal.getName();
        if (!order.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Bạn không có quyền hủy đơn hàng này");
        }
        if (!"PENDING".equals(order.getOrderStatus()) && !"CONFIRMED".equals(order.getOrderStatus())) {
            throw new RuntimeException("Không thể hủy đơn hàng ở trạng thái hiện tại");
        }
        String reason = body == null ? null : (String) body.get("reason");
        orderService.cancelOrder(id, reason);
        return ResponseEntity.ok(Map.of("success", true, "message", "Hủy đơn hàng thành công"));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Lỗi: " + e.getMessage()));
    }
}
```

## 3.6 UserProfileController + UserController(profile) -> ProfileApiController

Bo o file cu:
- POST /profile/update
- POST /profile/upload-avatar
- POST /profile/update-phone
- POST /user/profile/update
- POST /user/profile/change-password

Them o file moi:
- GET /api/v1/profile
- PUT /api/v1/profile
- POST /api/v1/profile/upload-avatar
- POST /api/v1/profile/update-phone
- POST /api/v1/profile/change-password

Code cu:
```java
@PostMapping("/update-phone")
public String updatePhone(@RequestParam String phone,
                          Principal principal,
                          RedirectAttributes redirectAttributes) {
    try {
        String email = getEmailFromPrincipal(principal);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        phoneVerificationService.updatePhoneAndVerify(user, phone);
        redirectAttributes.addFlashAttribute("success", "Cập nhật số điện thoại thành công!");
        if (phoneVerificationService.needsPhoneVerification(user)) {
            return "redirect:/profile";
        }
        return "redirect:/checkout";
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/profile";
    }
}
```

Code moi:
```java
@PostMapping("/update-phone")
public ResponseEntity<Map<String, Object>> updatePhone(@RequestBody Map<String, String> body,
                                                        Principal principal) {
    try {
        String email = getEmail(principal);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        phoneVerificationService.updatePhoneAndVerify(user, body.get("phone"));
        boolean stillNeedPhone = phoneVerificationService.needsPhoneVerification(user);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cập nhật số điện thoại thành công!",
                "next", stillNeedPhone ? "profile" : "checkout"
        ));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
    }
}
```

## 3.7 DashboardController -> DashboardAdminApiController

Bo o file cu:
- @GetMapping({"", "/", "/dashboard"})

Them o file moi:
- GET /api/v1/admin/dashboard

Code moi (giu nguyen DashboardService calls):
```java
@GetMapping
public ResponseEntity<Map<String, Object>> dashboard(@RequestParam(defaultValue = "month") String period) {
    try {
        Map<String, Object> data = new HashMap<>();
        data.put("revenue", dashboardService.getRevenue(period));
        data.put("orderCount", dashboardService.getOrderCount(period));
        data.put("productCount", dashboardService.getProductCount());
        data.put("userCount", dashboardService.getUserCount());
        data.put("recentOrders", dashboardService.getRecentOrders());
        data.put("orderStatsByStatus", dashboardService.getOrderStatsByStatus());
        data.put("orderStatsByBrand", dashboardService.getOrderStatsByBrand());
        data.put("revenueChartData", dashboardService.getRevenueChartData());
        data.put("brandChartData", dashboardService.getBrandChartData());
        data.put("orderGrowth", dashboardService.getOrderGrowthPercentage());
        data.put("selectedPeriod", period);
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    } catch (Exception e) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
    }
}
```

## 3.8 WatchAdminController -> WatchAdminApiController

Bo o file cu:
- POST /admin/watches/save
- POST /admin/watches/delete/{id}
- POST /admin/watches/toggle-active/{id}
- POST /admin/watches/update-stock/{id}

Them o file moi:
- POST /api/v1/admin/watches
- DELETE /api/v1/admin/watches/{id}
- PATCH /api/v1/admin/watches/{id}/toggle-active
- PATCH /api/v1/admin/watches/{id}/stock

Nguyen tac giu logic:
- Van dung watchRepo, watchImageRepo, fileUploadService, orderDetailRepo y nguyen.
- Van xu ly hasOrders thi inactive thay vi hard delete.

## 3.9 OrderAdminController -> OrderAdminApiController

Bo o file cu:
- POST /admin/orders/update-status
- POST /admin/orders/cancel/{id}

Them o file moi:
- PATCH /api/v1/admin/orders/{id}/status
- POST /api/v1/admin/orders/{id}/cancel

Code cu -> moi giu logic:
- validate transition status bang getValidNextStatuses(currentStatus).
- update order status + updatedDate.
- gui mail theo status (SHIPPING/DELIVERED/COMPLETED/CANCELLED).
- cancel goi orderService.cancelOrder(id, reason).

## 3.10 UserAdminController -> UserAdminApiController

Bo o file cu:
- POST /admin/users/ban/{id}
- POST /admin/users/unban/{id}
- POST /admin/users/save
- POST /admin/users/{id}/delete

Them o file moi:
- PATCH /api/v1/admin/users/{id}/ban
- PATCH /api/v1/admin/users/{id}/unban
- POST /api/v1/admin/users
- PUT /api/v1/admin/users/{id}
- DELETE /api/v1/admin/users/{id}

Logic giu nguyen:
- khong cho ban/delete ADMIN.
- tao moi user: check duplicate username/email, encode password.
- edit: giu password cu neu khong nhap password moi.
- role mapping giu nguyen (default USER neu roleNames rong).

## 3.11 BrandAdminController -> BrandAdminApiController

Bo o file cu:
- POST /admin/brands/save
- POST /admin/brands/{id}/delete
- POST /admin/brands/{id}/activate

Them o file moi:
- POST /api/v1/admin/brands
- PUT /api/v1/admin/brands/{id}
- DELETE /api/v1/admin/brands/{id}
- PATCH /api/v1/admin/brands/{id}/activate

Logic giu nguyen:
- check trung ten brand.
- delete: neu co watches thi soft delete (isActive=false), nguoc lai hard delete.

## 3.12 BankAdminController -> BankAdminApiController

Bo o file cu:
- GET /admin/bank-accounts/api/{id}
- POST /admin/bank-accounts/api/save
- DELETE /admin/bank-accounts/api/delete/{id}
- POST /admin/bank-accounts/add
- POST /admin/bank-accounts/edit/{id}
- GET /admin/bank-accounts/delete/{id}

Them o file moi:
- GET /api/v1/admin/bank-accounts/{id}
- POST /api/v1/admin/bank-accounts
- PUT /api/v1/admin/bank-accounts/{id}
- DELETE /api/v1/admin/bank-accounts/{id}

Logic giu nguyen:
- tao moi: set createdAt/updatedAt, save, generateAndSaveQrCode.
- update: cap nhat fields + generateAndSaveQrCode.
- delete: bankAccountService.delete(id).

## 3.13 PaymentAdminController -> PaymentAdminApiController

Bo o file cu:
- POST /admin/payments/methods/save
- POST /admin/payments/methods/toggle-active/{id}
- GET /admin/payments/transactions
- GET /admin/payments/transactions/{id}

Them o file moi:
- GET /api/v1/admin/payments/methods
- POST /api/v1/admin/payments/methods
- PATCH /api/v1/admin/payments/methods/{id}/toggle-active
- GET /api/v1/admin/payments/transactions
- GET /api/v1/admin/payments/transactions/{id}

Logic giu nguyen:
- save method: set createdDate/updatedDate.
- toggle active giu nguyen.
- transactions: filter status/methodId + paging + totalAmount.

## 3.14 InvoiceController -> InvoiceApiController (tao moi)

Bo o file cu:
- GET /invoice/{orderId}/word
- GET /invoice/{orderId}/pdf

Them o file moi:
- GET /api/v1/invoices/{orderId}/word
- GET /api/v1/invoices/{orderId}/pdf

Code moi (logic byte[] giu nguyen):
```java
@RestController
@RequestMapping("/api/v1/invoices")
public class InvoiceApiController {

    @Autowired
    private InvoiceService invoiceService;

    @GetMapping("/{orderId}/word")
    public ResponseEntity<byte[]> downloadWordInvoice(@PathVariable Integer orderId) {
        try {
            byte[] content = invoiceService.generateWordInvoice(orderId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + orderId + ".docx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{orderId}/pdf")
    public ResponseEntity<byte[]> downloadPdfInvoice(@PathVariable Integer orderId) {
        try {
            byte[] content = invoiceService.generatePdfInvoice(orderId);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + orderId + ".pdf")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
```

## 3.15 PaymentController -> PaymentApiController (tao moi)

Bo o file cu:
- GET /payment/vnpay-return

Them o file moi:
- GET /api/v1/payments/vnpay-return

Logic giu nguyen:
- verifyPayment(params)
- update PaymentTransaction
- neu responseCode = 00 thi updateStatus CONFIRMED
- mapping thong diep loi theo response code

Luu y:
- Neu cong thanh toan da callback den /payment/vnpay-return, can giu route cu tam thoi va them route moi song song den khi doi cau hinh VNPay.

## 3.16 AccountController -> AccountApiController (tao moi)

Bo o file cu:
- GET /account
- GET /account/orders
- GET /account/change-password

Them o file moi:
- GET /api/v1/account
- GET /api/v1/account/orders
- GET /api/v1/account/change-password

Code moi (giu dung y nghia redirect route cu):
```java
@RestController
@RequestMapping("/api/v1/account")
public class AccountApiController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> account() {
        return ResponseEntity.ok(Map.of("success", true, "redirectTo", "/profile"));
    }

    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> orders() {
        return ResponseEntity.ok(Map.of("success", true, "redirectTo", "/orders"));
    }

    @GetMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword() {
        return ResponseEntity.ok(Map.of("success", true, "redirectTo", "/profile"));
    }
}
```

## 4. Thu tu thuc hien khong vo logic

1. Hoan thien ProductApiController + AuthApiController
2. Hoan thien CartApiController
3. Hoan thien CheckoutApiController + OrderApiController
4. Hoan thien ProfileApiController
5. Hoan thien admin API controllers
6. Tao InvoiceApiController + PaymentApiController + AccountApiController
7. FE doi endpoint sang /api/v1/**
8. Sau khi FE on dinh moi xoa method MVC cu

## 5. Checklist doi chieu truoc khi xoa code MVC

- [ ] Moi URI cu da co URI moi 1-1
- [ ] Service calls trong code moi trung voi code cu
- [ ] Test Swagger pass cho URI moi
- [ ] FE khong con goi URI cu
- [ ] SecurityConfig da role dung /api/v1/admin/**
- [ ] Build khong loi

## 6. Quy tac de dam bao KHONG doi logic

- Copy nguyen khoi try/catch va noi dung throw RuntimeException.
- Copy nguyen khoi validate input.
- Copy nguyen service method call (ten ham + tham so).
- Chi doi:
  - @Controller -> @RestController
  - @ModelAttribute/@RequestParam form -> @RequestBody JSON (neu endpoint ghi)
  - String view/redirect -> ResponseEntity<Map<String,Object>>

Ket qua: nghiep vu giu nguyen, chi doi giao tiep de React su dung.

## 7. Ket qua check thuc te tren code hien tai (2026-04-01)

Trang thai tong quan:
- Da co API file: 13/16 (thieu 3 file moi: InvoiceApiController, PaymentApiController, AccountApiController)
- Da implement logic that su: 1/13 (CartApiController)
- Dang o muc scaffold TODO: 12/13

## 7.1 Nhom Public/Auth/Product

- PublicController van giu day du flow MVC:
    - /register: co [PublicController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/PublicController.java#L292)
    - /confirm-register: co [PublicController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/PublicController.java#L347)
    - /resend-verification: co [PublicController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/PublicController.java#L377)
- AuthApiController chua day du:
    - co /register, /confirm-register, /forgot-password, /reset-password, /me
    - thieu /resend-verification
    - cac endpoint chinh dang TODO: [AuthApiController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/api/AuthApiController.java#L25)
- ProductApiController day du URI nhung dang TODO:
    - [ProductApiController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/api/ProductApiController.java#L31)

## 7.2 Nhom Cart

- CartController hien tai chi con view GET /cart (tot cho giai doan chuyen tiep):
    - [CartController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/CartController.java#L20)
- CartApiController da co logic that (khong phai TODO):
    - [CartApiController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/api/CartApiController.java#L29)
    - [CartApiController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/api/CartApiController.java#L60)

## 7.3 Nhom Checkout/Order/Profile

- UserController van giu MVC cho checkout/orders/profile:
    - [UserController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/UserController.java#L135)
    - [UserController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/UserController.java#L170)
    - [UserController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/UserController.java#L253)
    - [UserController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/UserController.java#L320)
- CheckoutApiController dang TODO:
    - [CheckoutApiController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/api/CheckoutApiController.java#L24)
- OrderApiController dang TODO:
    - [OrderApiController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/api/OrderApiController.java#L28)
- ProfileApiController moi co change-password, thieu GET/PUT/upload-avatar/update-phone:
    - [ProfileApiController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/api/ProfileApiController.java#L44)
- UserProfileController van giu update/upload/update-phone:
    - [UserProfileController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/UserProfileController.java#L86)
    - [UserProfileController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/UserProfileController.java#L117)
    - [UserProfileController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/UserProfileController.java#L140)

## 7.4 Nhom Admin

- Tat ca admin API files hien dang scaffold TODO (chua copy logic tu MVC):
    - dashboard: [DashboardAdminApiController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/api/admin/DashboardAdminApiController.java#L22)
    - watches: [WatchAdminApiController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/api/admin/WatchAdminApiController.java#L32)
    - orders: [OrderAdminApiController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/api/admin/OrderAdminApiController.java#L31)
    - users: [UserAdminApiController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/api/admin/UserAdminApiController.java#L29)
    - brands: [BrandAdminApiController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/api/admin/BrandAdminApiController.java#L27)
    - bank accounts: [BankAdminApiController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/api/admin/BankAdminApiController.java#L27)
    - payments: [PaymentAdminApiController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/api/admin/PaymentAdminApiController.java#L25)

## 7.5 Cac diem lech mapping can sua ngay

1. Auth:
- MVC co /resend-verification, API chua co endpoint tuong ung.

2. Brand admin:
- MVC co /{id}/activate: [BrandAdminController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/admin/BrandAdminController.java#L133)
- API chua co PATCH /{id}/activate.

3. Bank admin:
- MVC co GET /api/{id}: [BankAdminController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/admin/BankAdminController.java#L35)
- API hien tai chua co GET /api/v1/admin/bank-accounts/{id}.

4. Payment admin:
- MVC co save/toggle/detail:
    - [PaymentAdminController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/admin/PaymentAdminController.java#L79)
    - [PaymentAdminController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/admin/PaymentAdminController.java#L105)
    - [PaymentAdminController.java](../2BShop/src/main/java/boiz/shop/_2BShop/controller/admin/PaymentAdminController.java#L169)
- API hien tai moi co 2 GET (methods, transactions), thieu POST/PATCH/GET detail.

5. User admin:
- MVC co save/delete/new/edit, API hien tai moi co list/ban/unban.

## 8. Ke hoach chuyen doi de dung ngay (khong doi logic)

1. Hoan thien ProductApiController bang copy logic tu PublicController (list/newest/discount/detail).
2. Hoan thien AuthApiController (register/confirm/resend/forgot/reset) copy tu PublicController + PasswordResetController.
3. Hoan thien CheckoutApiController + OrderApiController copy tu CheckoutController + UserController + OrderTrackingController.
4. Mo rong ProfileApiController copy tu UserProfileController.
5. Hoan thien tung admin ApiController copy y nguyen service calls tu admin MVC.
6. Tao 3 file API con thieu: InvoiceApiController, PaymentApiController, AccountApiController.
7. Chuyen FE sang /api/v1/** roi moi xoa methods MVC da doi.
