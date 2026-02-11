# 💳 HƯỚNG DẪN TÍCH HỢP VNPAY - 2BSHOP

## ✅ ĐÃ HOÀN THÀNH

### **1. Files đã tạo:**
- ✅ `VNPayService.java` - Service xử lý VNPay API
- ✅ `PaymentController.java` - Controller xử lý callback
- ✅ `payment-result.html` - Trang hiển thị kết quả thanh toán
- ✅ Config trong `application.properties`

---

## 🚀 CÁC BƯỚC TIẾP THEO

### **BƯỚC 1: ĐĂNG KÝ VNPAY SANDBOX** ⏳

1. Truy cập: **https://sandbox.vnpayment.vn/devreg/**

2. Điền form đăng ký:
   ```
   Merchant Name: 2BShop
   Return URL: http://localhost:8080/payment/vnpay-return
   Email: [Email của bạn]
   Phone: [SĐT của bạn]
   ```

3. Submit form và đợi email từ VNPay (thường 5-10 phút)

4. Email sẽ chứa:
   - **TMN Code** (ví dụ: VNPAYMERCHANT123)
   - **Hash Secret** (ví dụ: ABCDEF1234567890...)

---

### **BƯỚC 2: CẬP NHẬT APPLICATION.PROPERTIES** 📝

Sau khi nhận được email từ VNPay, mở file:
```
d:\BoizShop\2BShop\src\main\resources\application.properties
```

Tìm và thay thế:
```properties
# VNPay Configuration
vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
vnpay.returnUrl=http://localhost:8080/payment/vnpay-return
vnpay.tmnCode=YOUR_TMN_CODE_FROM_EMAIL        # ← Thay bằng TMN Code thật
vnpay.hashSecret=YOUR_HASH_SECRET_FROM_EMAIL  # ← Thay bằng Hash Secret thật
```

**VÍ DỤ:**
```properties
vnpay.tmnCode=VNPAY12345678
vnpay.hashSecret=ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890ABCD
```

---

### **BƯỚC 3: CODE VNPAYSERVICE.JAVA**

**File:** `src/main/java/boiz/shop/_2BShop/service/VNPayService.java`

```java
package boiz.shop._2BShop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class VNPayService {

    @Value("${vnpay.url}")
    private String vnpayUrl;

    @Value("${vnpay.returnUrl}")
    private String returnUrl;

    @Value("${vnpay.tmnCode}")
    private String tmnCode;

    @Value("${vnpay.hashSecret}")
    private String hashSecret;

    /**
     * Tạo URL thanh toán VNPay
     * @param orderId Mã đơn hàng
     * @param amount Số tiền (VND)
     * @param ipAddress IP của khách hàng
     * @return URL redirect đến VNPay
     */
    public String createPaymentUrl(Integer orderId, Long amount, String ipAddress) throws Exception {
        Map<String, String> vnpParams = new HashMap<>();
        
        // Thông tin cơ bản
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", tmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay tính bằng đồng (x100)
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", String.valueOf(orderId)); // Mã đơn hàng
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang #" + orderId);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", returnUrl);
        vnpParams.put("vnp_IpAddr", ipAddress);
        
        // Thời gian
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);
        
        cld.add(Calendar.MINUTE, 15); // Expire sau 15 phút
        String vnpExpireDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_ExpireDate", vnpExpireDate);
        
        // Sắp xếp params theo alphabet
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);
        
        // Build hash data và query string
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        
        for (String fieldName : fieldNames) {
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Build hash data
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                
                // Build query string
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII))
                     .append('=')
                     .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                
                if (fieldNames.indexOf(fieldName) < fieldNames.size() - 1) {
                    hashData.append('&');
                    query.append('&');
                }
            }
        }
        
        // Tạo secure hash
        String vnpSecureHash = hmacSHA512(hashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnpSecureHash);
        
        // URL hoàn chỉnh
        return vnpayUrl + "?" + query.toString();
    }
    
    /**
     * Verify callback từ VNPay
     * @param params Parameters từ VNPay callback
     * @return true nếu hợp lệ
     */
    public boolean verifyPayment(Map<String, String> params) {
        String vnpSecureHash = params.get("vnp_SecureHash");
        params.remove("vnp_SecureHash");
        params.remove("vnp_SecureHashType");
        
        // Sắp xếp params
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        
        // Build hash data
        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=').append(fieldValue);
                if (fieldNames.indexOf(fieldName) < fieldNames.size() - 1) {
                    hashData.append('&');
                }
            }
        }
        
        // Verify hash
        String calculatedHash = hmacSHA512(hashSecret, hashData.toString());
        return calculatedHash.equals(vnpSecureHash);
    }
    
    /**
     * HMAC SHA512
     */
    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA512", e);
        }
    }
}
```

---

### **BƯỚC 4: CODE PAYMENTCONTROLLER.JAVA**

**File:** `src/main/java/boiz/shop/_2BShop/controller/PaymentController.java`

```java
package boiz.shop._2BShop.controller;

import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.entity.PaymentTransaction;
import boiz.shop._2BShop.respository.OrderRepository;
import boiz.shop._2BShop.respository.PaymentTransactionRepository;
import boiz.shop._2BShop.service.VNPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    /**
     * VNPay Return URL - Xử lý callback sau khi thanh toán
     */
    @GetMapping("/vnpay-return")
    public String vnpayReturn(@RequestParam Map<String, String> params, Model model) {
        
        // Verify signature
        boolean isValid = vnPayService.verifyPayment(params);
        
        if (!isValid) {
            model.addAttribute("success", false);
            model.addAttribute("message", "Chữ ký không hợp lệ!");
            return "payment-result";
        }
        
        // Lấy thông tin thanh toán
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");
        String orderId = params.get("vnp_TxnRef");
        String amount = params.get("vnp_Amount");
        String payDate = params.get("vnp_PayDate");
        
        // Tính số tiền (VNPay trả về x100)
        Long amountValue = Long.parseLong(amount) / 100;
        
        // Format pay date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        LocalDateTime paymentDate = LocalDateTime.parse(payDate, formatter);
        
        // Lấy order
        Order order = orderRepository.findById(Integer.parseInt(orderId))
            .orElseThrow(() -> new RuntimeException("Order not found"));
        
        if ("00".equals(responseCode)) {
            // Thanh toán thành công
            
            // Update order status
            order.setOrderStatus("CONFIRMED");
            order.setUpdatedDate(LocalDateTime.now());
            orderRepository.save(order);
            
            // Update payment transaction
            PaymentTransaction transaction = paymentTransactionRepository.findByOrder(order)
                .orElse(new PaymentTransaction());
            
            transaction.setOrder(order);
            transaction.setTransactionCode(transactionNo);
            transaction.setStatus("SUCCESS");
            transaction.setTransactionDate(paymentDate);
            transaction.setResponseData(params.toString());
            paymentTransactionRepository.save(transaction);
            
            // Model for success page
            model.addAttribute("success", true);
            model.addAttribute("message", "Thanh toán thành công!");
            model.addAttribute("orderId", "ORD" + String.format("%06d", order.getOrderId()));
            model.addAttribute("amount", String.format("%,d", amountValue));
            model.addAttribute("transactionNo", transactionNo);
            model.addAttribute("payDate", paymentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            
        } else {
            // Thanh toán thất bại
            
            // Update order status
            order.setOrderStatus("CANCELLED");
            order.setUpdatedDate(LocalDateTime.now());
            orderRepository.save(order);
            
            // Update payment transaction
            PaymentTransaction transaction = paymentTransactionRepository.findByOrder(order)
                .orElse(new PaymentTransaction());
            
            transaction.setOrder(order);
            transaction.setStatus("FAILED");
            transaction.setTransactionDate(LocalDateTime.now());
            transaction.setResponseData(params.toString());
            paymentTransactionRepository.save(transaction);
            
            // Model for error page
            model.addAttribute("success", false);
            model.addAttribute("message", getErrorMessage(responseCode));
            model.addAttribute("orderId", "ORD" + String.format("%06d", order.getOrderId()));
        }
        
        return "payment-result";
    }
    
    /**
     * Map response code to error message
     */
    private String getErrorMessage(String responseCode) {
        switch (responseCode) {
            case "07": return "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường).";
            case "09": return "Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng.";
            case "10": return "Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần.";
            case "11": return "Đã hết hạn chờ thanh toán. Xin quý khách vui lòng thực hiện lại giao dịch.";
            case "12": return "Thẻ/Tài khoản của khách hàng bị khóa.";
            case "13": return "Quý khách nhập sai mật khẩu xác thực giao dịch (OTP). Xin quý khách vui lòng thực hiện lại giao dịch.";
            case "24": return "Khách hàng hủy giao dịch.";
            case "51": return "Tài khoản của quý khách không đủ số dư để thực hiện giao dịch.";
            case "65": return "Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày.";
            case "75": return "Ngân hàng thanh toán đang bảo trì.";
            case "79": return "KH nhập sai mật khẩu thanh toán quá số lần quy định. Xin quý khách vui lòng thực hiện lại giao dịch";
            default: return "Giao dịch thất bại. Vui lòng thử lại sau.";
        }
    }
}
```

---

### **BƯỚC 5: CODE PAYMENT-RESULT.HTML**

**File:** `src/main/resources/templates/payment-result.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      layout:decorate="~{layout/base-layout}">
<head>
    <title>Kết quả thanh toán</title>
    <style>
        .payment-result-container {
            max-width: 600px;
            margin: 50px auto;
            padding: 40px;
            text-align: center;
            background: white;
            border-radius: 10px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.1);
        }
        
        .success-icon {
            width: 80px;
            height: 80px;
            border-radius: 50%;
            background: #28a745;
            color: white;
            font-size: 40px;
            line-height: 80px;
            margin: 0 auto 20px;
        }
        
        .error-icon {
            width: 80px;
            height: 80px;
            border-radius: 50%;
            background: #dc3545;
            color: white;
            font-size: 40px;
            line-height: 80px;
            margin: 0 auto 20px;
        }
        
        .result-title {
            font-size: 28px;
            font-weight: 700;
            margin-bottom: 10px;
        }
        
        .result-message {
            font-size: 16px;
            color: #666;
            margin-bottom: 30px;
        }
        
        .result-details {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 30px;
            text-align: left;
        }
        
        .detail-row {
            display: flex;
            justify-content: space-between;
            padding: 10px 0;
            border-bottom: 1px solid #dee2e6;
        }
        
        .detail-row:last-child {
            border-bottom: none;
        }
        
        .detail-label {
            font-weight: 600;
            color: #333;
        }
        
        .detail-value {
            color: #666;
        }
        
        .amount-value {
            color: #dc3545;
            font-size: 24px;
            font-weight: 700;
        }
        
        .btn-group {
            display: flex;
            gap: 15px;
            justify-content: center;
        }
        
        .btn {
            padding: 12px 30px;
            border-radius: 5px;
            text-decoration: none;
            font-weight: 600;
            transition: all 0.3s;
        }
        
        .btn-primary {
            background: #000;
            color: white;
        }
        
        .btn-primary:hover {
            background: #333;
            transform: translateY(-2px);
        }
        
        .btn-secondary {
            background: #6c757d;
            color: white;
        }
        
        .btn-secondary:hover {
            background: #5a6268;
            transform: translateY(-2px);
        }
    </style>
</head>
<body>
    <div layout:fragment="content">
        <div class="payment-result-container">
            <!-- Success Icon -->
            <div th:if="${success}" class="success-icon">✓</div>
            <div th:unless="${success}" class="error-icon">✗</div>
            
            <!-- Title -->
            <h1 class="result-title" th:text="${success ? 'Thanh toán thành công!' : 'Thanh toán thất bại!'}">
                Thanh toán thành công!
            </h1>
            
            <!-- Message -->
            <p class="result-message" th:text="${message}">
                Cảm ơn bạn đã mua hàng tại BOIZ SHOP
            </p>
            
            <!-- Payment Details (Only show on success) -->
            <div th:if="${success}" class="result-details">
                <div class="detail-row">
                    <span class="detail-label">Mã đơn hàng:</span>
                    <span class="detail-value" th:text="${orderId}">ORD000001</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Số tiền:</span>
                    <span class="amount-value" th:text="${amount} + '₫'">1,000,000₫</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Mã giao dịch:</span>
                    <span class="detail-value" th:text="${transactionNo}">14123456</span>
                </div>
                <div class="detail-row">
                    <span class="detail-label">Thời gian:</span>
                    <span class="detail-value" th:text="${payDate}">14/01/2026 10:30:00</span>
                </div>
            </div>
            
            <!-- Error Details -->
            <div th:unless="${success}" class="result-details">
                <div class="detail-row">
                    <span class="detail-label">Mã đơn hàng:</span>
                    <span class="detail-value" th:text="${orderId}">ORD000001</span>
                </div>
            </div>
            
            <!-- Buttons -->
            <div class="btn-group">
                <a href="/" class="btn btn-secondary">Về trang chủ</a>
                <a th:if="${success}" href="/user/orders" class="btn btn-primary">Xem đơn hàng</a>
                <a th:unless="${success}" href="/cart" class="btn btn-primary">Giỏ hàng</a>
            </div>
        </div>
    </div>
</body>
</html>
```

---

### **BƯỚC 6: TÍCH HỢP VÀO CHECKOUT**

**Update CheckoutController hoặc UserController:**

```java
@Autowired
private VNPayService vnPayService;

@PostMapping("/checkout")
public String checkout(
    @RequestParam String fullName,
    @RequestParam String phone,
    @RequestParam String address,
    @RequestParam(required = false) String note,
    @RequestParam String paymentMethod,
    HttpServletRequest request,
    RedirectAttributes redirectAttributes
) {
    try {
        // Tạo order
        Order order = orderService.createOrder(fullName, phone, address, note, paymentMethod);
        
        // Nếu chọn VNPay → Redirect đến VNPay
        if ("VNPAY".equals(paymentMethod)) {
            String ipAddress = getIpAddress(request);
            Long amount = order.getTotalAmount().longValue();
            
            String paymentUrl = vnPayService.createPaymentUrl(
                order.getOrderId(), 
                amount, 
                ipAddress
            );
            
            return "redirect:" + paymentUrl;
        }
        
        // COD → Success page
        return "redirect:/order-success?orderId=" + order.getOrderId();
        
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", e.getMessage());
        return "redirect:/cart";
    }
}

// Helper method lấy IP
private String getIpAddress(HttpServletRequest request) {
    String ipAddress = request.getHeader("X-FORWARDED-FOR");
    if (ipAddress == null) {
        ipAddress = request.getRemoteAddr();
    }
    return ipAddress;
}
```

---

### **BƯỚC 7: TEST THANH TOÁN** 🧪

#### **Thẻ test VNPay Sandbox:**

```
╔════════════════════════════════════════════╗
║   THÔNG TIN THẺ TEST VNPAY SANDBOX         ║
╠════════════════════════════════════════════╣
║ Ngân hàng:    NCB (Ngân hàng Quốc Dân)    ║
║ Số thẻ:       9704198526191432198          ║
║ Tên chủ thẻ:  NGUYEN VAN A                 ║
║ Ngày phát hành: 07/15                      ║
║ Mật khẩu OTP: 123456                       ║
╚════════════════════════════════════════════╝
```

#### **Test Flow:**

1. **Khởi động Spring Boot:**
   ```bash
   cd d:\BoizShop\2BShop
   mvn spring-boot:run
   ```

2. **Truy cập:** http://localhost:8080

3. **Thêm sản phẩm vào giỏ hàng**

4. **Checkout và chọn phương thức: VNPay**

5. **Hệ thống redirect đến trang VNPay**

6. **Đăng nhập VNPay Sandbox** với thẻ test trên

7. **Nhập OTP: 123456**

8. **VNPay redirect về:** http://localhost:8080/payment/vnpay-return

9. **Xem kết quả thanh toán**

---

## 🐛 TROUBLESHOOTING

### **1. Lỗi "Invalid signature"**
**Nguyên nhân:** Hash Secret sai

**Giải pháp:** 
- Kiểm tra lại Hash Secret trong email VNPay
- Copy chính xác vào application.properties

### **2. Lỗi "Invalid TMN Code"**
**Nguyên nhân:** TMN Code sai hoặc chưa active

**Giải pháp:** 
- Kiểm tra lại email VNPay
- Đợi account active (có thể mất vài phút)

### **3. Lỗi "Return URL not match"**
**Nguyên nhân:** Return URL trong code khác với lúc đăng ký

**Giải pháp:**
- Cập nhật lại Return URL trong VNPay Sandbox
- Hoặc đổi URL trong `application.properties`

### **4. Không nhận được callback**
**Nguyên nhân:** Spring Boot không chạy

**Giải pháp:**
- Kiểm tra Spring Boot đang chạy: http://localhost:8080
- Check logs trong console

---

## 📊 DATABASE

### **Kiểm tra Payment Transactions:**

```sql
SELECT * FROM payment_transactions;
```

**Kết quả sau khi thanh toán thành công:**
```
transaction_id | order_id | transaction_code | payment_method_id | amount      | status  | transaction_date
---------------|----------|------------------|-------------------|-------------|---------|------------------
1              | 1        | 14123456         | 2 (VNPAY)         | 1000000.00  | SUCCESS | 2026-01-14 10:30:00
```

---

## 🎯 FLOW HOÀN CHỈNH

```
┌─────────────┐
│   User      │
│  Checkout   │
└──────┬──────┘
       │ (1) Chọn VNPay
       ▼
┌─────────────────────┐
│  VNPayService       │
│  createPaymentUrl() │
└──────┬──────────────┘
       │ (2) Redirect đến VNPay
       ▼
┌─────────────────────┐
│  VNPay Sandbox      │
│  (Nhập thẻ test)    │
└──────┬──────────────┘
       │ (3) Thanh toán xong
       ▼
┌─────────────────────────────┐
│  PaymentController          │
│  /payment/vnpay-return      │
│  - Verify signature         │
│  - Update order status      │
│  - Update payment_transaction│
└──────┬──────────────────────┘
       │ (4) Hiển thị kết quả
       ▼
┌─────────────────────┐
│  payment-result.html│
│  ✅ Thanh toán      │
│     thành công!     │
└─────────────────────┘
```

---

## 📞 HỖ TRỢ

### **VNPay Support:**
- Email: support@vnpay.vn
- Hotline: 1900 55 55 77
- Docs: https://sandbox.vnpayment.vn/apis/

---

## ✅ CHECKLIST HOÀN THÀNH

- [x] Tạo VNPayService.java
- [x] Tạo PaymentController.java
- [x] Tạo payment-result.html
- [x] Config application.properties
- [ ] **Đăng ký VNPay Sandbox** ← BẠN CẦN LÀM BƯỚC NÀY
- [ ] **Cập nhật TMN Code & Hash Secret trong application.properties**
- [ ] **Test thanh toán với thẻ NCB test**

---

**👉 BÂY GIỜ HÃY ĐĂNG KÝ VNPAY SANDBOX TẠI: https://sandbox.vnpayment.vn/devreg/**
