# 📧 HƯỚNG DẪN GỬI EMAIL XÁC NHẬN ĐƠN HÀNG

## ✅ ĐÃ HOÀN THÀNH

Hệ thống gửi email hóa đơn tự động khi đặt hàng thành công đã được tích hợp hoàn chỉnh!

---

## 🎯 CHỨC NĂNG ĐÃ THÊM

### **1. Method mới trong MailService**
- **Method**: `sendOrderConfirmation(Order order, List<OrderDetail> orderDetails)`
- **File**: `src/main/java/boiz/shop/_2BShop/service/MailService.java`

### **2. Tích hợp vào OrderService**
- **File**: `src/main/java/boiz/shop/_2BShop/service/OrderService.java`
- **Method**: `createOrder()` - Tự động gửi email sau khi tạo đơn hàng thành công

---

## 📋 THÔNG TIN TRONG EMAIL HÓA ĐƠN

### **A. THÔNG TIN ĐƠN HÀNG**
- ✅ Mã đơn hàng (format: ORD000001, ORD000002,...)
- ✅ Ngày đặt hàng (format: dd/MM/yyyy HH:mm)
- ✅ Trạng thái đơn hàng
- ✅ Phương thức thanh toán

### **B. CHI TIẾT SẢN PHẨM (Table format)**
Mỗi sản phẩm hiển thị:
- ✅ **Mã sản phẩm**: W00001, W00002,... (format từ watch_id)
- ✅ **Tên sản phẩm**: Tên đầy đủ của đồng hồ
- ✅ **Thương hiệu**: Tên brand (Rolex, Omega,...)
- ✅ **Mô tả sản phẩm**: Hiển thị 80 ký tự đầu tiên (nếu có)
- ✅ **Số lượng**: Quantity
- ✅ **Đơn giá**: Giá gốc (unit_price)
- ✅ **Giảm giá**: Số tiền giảm (discount_amount) - màu đỏ
- ✅ **Thành tiền**: Subtotal (tổng sau giảm)

### **C. TỔNG KẾT ĐƠN HÀNG**
- ✅ Tạm tính (subtotal)
- ✅ Phí vận chuyển (hiển thị "Miễn phí" nếu = 0)
- ✅ **TỔNG CỘNG** (màu đỏ, font lớn)

### **D. THÔNG TIN GIAO HÀNG**
- ✅ Người nhận
- ✅ Số điện thoại
- ✅ Địa chỉ giao hàng
- ✅ Ghi chú (nếu có)

### **E. PHƯƠNG THỨC THANH TOÁN**
- ✅ Tên phương thức (COD, Banking, Credit Card,...)

---

## 🎨 THIẾT KẾ EMAIL

### **1. Header**
- Background: Gradient xanh navy (#1a1a2e → #16213e)
- Logo: "BOIZ SHOP" - chữ trắng, letter-spacing 4px
- Badge: "✓ ĐẶT HÀNG THÀNH CÔNG" (màu xanh lá)

### **2. Content**
- **Order Info Box**: Background xám nhạt, border trái xanh dương
- **Products Table**: Header đen, rows có border
- **Summary Box**: Background xám nhạt, total row nổi bật
- **Shipping Info Box**: Background vàng nhạt, border vàng

### **3. Footer**
- Background: Xanh navy (#1a1a2e)
- Thông tin liên hệ: Hotline, Email, Địa chỉ, Giờ làm việc
- Copyright © 2026

### **4. Button CTA**
- "XEM CHI TIẾT ĐƠN HÀNG" (màu xanh dương)
- Link: `http://localhost:8080/account/orders`

---

## 💻 CODE MailService.sendOrderConfirmation()

**File:** `src/main/java/boiz/shop/_2BShop/service/MailService.java`

```java
package boiz.shop._2BShop.service;

import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.entity.OrderDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Gửi email xác nhận đơn hàng
     */
    public void sendOrderConfirmation(Order order, List<OrderDetail> orderDetails) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // Email subject
        String subject = "[BOIZ SHOP] Xác nhận đơn hàng #" + formatOrderId(order.getOrderId());
        
        // Email content
        String htmlContent = buildOrderConfirmationEmail(order, orderDetails);

        helper.setTo(order.getUser().getEmail());
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        helper.setFrom("noreply@boizshop.com");

        mailSender.send(message);
    }

    /**
     * Build HTML email content
     */
    private String buildOrderConfirmationEmail(Order order, List<OrderDetail> orderDetails) {
        StringBuilder html = new StringBuilder();
        DecimalFormat formatter = new DecimalFormat("#,###");

        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; background: #fff; }");
        html.append(".header { background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); padding: 30px; text-align: center; }");
        html.append(".logo { font-size: 32px; font-weight: bold; color: white; letter-spacing: 4px; margin-bottom: 15px; }");
        html.append(".badge { background: #28a745; color: white; padding: 8px 20px; border-radius: 20px; font-size: 14px; display: inline-block; }");
        html.append(".content { padding: 30px; }");
        html.append(".info-box { background: #f8f9fa; padding: 20px; border-left: 4px solid #007bff; margin-bottom: 20px; }");
        html.append(".info-row { display: flex; justify-content: space-between; margin-bottom: 10px; }");
        html.append(".label { font-weight: bold; color: #555; }");
        html.append(".value { color: #333; }");
        html.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
        html.append("th { background: #000; color: white; padding: 12px; text-align: left; }");
        html.append("td { padding: 12px; border-bottom: 1px solid #ddd; }");
        html.append(".product-name { font-weight: bold; color: #007bff; }");
        html.append(".brand { color: #666; font-size: 14px; }");
        html.append(".discount { color: #dc3545; font-weight: bold; }");
        html.append(".summary-box { background: #f8f9fa; padding: 20px; margin: 20px 0; }");
        html.append(".total-row { font-size: 20px; font-weight: bold; color: #dc3545; margin-top: 10px; padding-top: 10px; border-top: 2px solid #000; }");
        html.append(".shipping-box { background: #fff3cd; border: 2px solid #ffc107; padding: 20px; margin: 20px 0; }");
        html.append(".footer { background: #1a1a2e; color: white; padding: 30px; text-align: center; }");
        html.append(".footer a { color: #007bff; text-decoration: none; }");
        html.append(".btn { background: #007bff; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; display: inline-block; margin: 20px 0; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='container'>");

        // Header
        html.append("<div class='header'>");
        html.append("<div class='logo'>BOIZ SHOP</div>");
        html.append("<span class='badge'>✓ ĐẶT HÀNG THÀNH CÔNG</span>");
        html.append("</div>");

        // Content
        html.append("<div class='content'>");
        html.append("<h2>Xin chào ").append(order.getReceiverName()).append(",</h2>");
        html.append("<p>Cảm ơn bạn đã đặt hàng tại BOIZ SHOP. Đơn hàng của bạn đã được tiếp nhận và đang được xử lý.</p>");

        // Order Info
        html.append("<div class='info-box'>");
        html.append("<h3 style='margin-top: 0;'>📋 Thông tin đơn hàng</h3>");
        html.append("<div class='info-row'>");
        html.append("<span class='label'>Mã đơn hàng:</span>");
        html.append("<span class='value'>").append(formatOrderId(order.getOrderId())).append("</span>");
        html.append("</div>");
        html.append("<div class='info-row'>");
        html.append("<span class='label'>Ngày đặt:</span>");
        html.append("<span class='value'>").append(order.getOrderDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("</span>");
        html.append("</div>");
        html.append("<div class='info-row'>");
        html.append("<span class='label'>Trạng thái:</span>");
        html.append("<span class='value'>").append(getStatusText(order.getOrderStatus())).append("</span>");
        html.append("</div>");
        html.append("<div class='info-row'>");
        html.append("<span class='label'>Phương thức thanh toán:</span>");
        html.append("<span class='value'>").append(order.getPaymentMethod().getMethodName()).append("</span>");
        html.append("</div>");
        html.append("</div>");

        // Products Table
        html.append("<h3>🛍️ Chi tiết sản phẩm</h3>");
        html.append("<table>");
        html.append("<thead>");
        html.append("<tr>");
        html.append("<th>Sản phẩm</th>");
        html.append("<th style='text-align: center;'>SL</th>");
        html.append("<th style='text-align: right;'>Đơn giá</th>");
        html.append("<th style='text-align: right;'>Thành tiền</th>");
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");

        for (OrderDetail detail : orderDetails) {
            html.append("<tr>");
            html.append("<td>");
            html.append("<div class='product-name'>").append(detail.getWatch().getWatchName()).append("</div>");
            html.append("<div class='brand'>Mã: ").append(formatWatchId(detail.getWatch().getWatchId())).append(" | ");
            html.append("Thương hiệu: ").append(detail.getWatch().getBrand().getBrandName()).append("</div>");
            if (detail.getWatch().getDescription() != null && !detail.getWatch().getDescription().isEmpty()) {
                String desc = detail.getWatch().getDescription();
                if (desc.length() > 80) {
                    desc = desc.substring(0, 80) + "...";
                }
                html.append("<div style='font-size: 12px; color: #888;'>").append(desc).append("</div>");
            }
            html.append("</td>");
            html.append("<td style='text-align: center;'>").append(detail.getQuantity()).append("</td>");
            html.append("<td style='text-align: right;'>");
            html.append(formatter.format(detail.getUnitPrice())).append("₫");
            if (detail.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                html.append("<br><span class='discount'>-").append(formatter.format(detail.getDiscountAmount())).append("₫</span>");
            }
            html.append("</td>");
            html.append("<td style='text-align: right; font-weight: bold;'>").append(formatter.format(detail.getSubtotal())).append("₫</td>");
            html.append("</tr>");
        }

        html.append("</tbody>");
        html.append("</table>");

        // Summary
        BigDecimal subtotal = orderDetails.stream()
            .map(OrderDetail::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal shippingFee = order.getTotalAmount().subtract(subtotal);

        html.append("<div class='summary-box'>");
        html.append("<div class='info-row'>");
        html.append("<span>Tạm tính:</span>");
        html.append("<span>").append(formatter.format(subtotal)).append("₫</span>");
        html.append("</div>");
        html.append("<div class='info-row'>");
        html.append("<span>Phí vận chuyển:</span>");
        if (shippingFee.compareTo(BigDecimal.ZERO) == 0) {
            html.append("<span style='color: #28a745; font-weight: bold;'>Miễn phí</span>");
        } else {
            html.append("<span>").append(formatter.format(shippingFee)).append("₫</span>");
        }
        html.append("</div>");
        html.append("<div class='total-row'>");
        html.append("<span>TỔNG CỘNG:</span>");
        html.append("<span>").append(formatter.format(order.getTotalAmount())).append("₫</span>");
        html.append("</div>");
        html.append("</div>");

        // Shipping Info
        html.append("<div class='shipping-box'>");
        html.append("<h3 style='margin-top: 0;'>🚚 Thông tin giao hàng</h3>");
        html.append("<div class='info-row'>");
        html.append("<span class='label'>Người nhận:</span>");
        html.append("<span>").append(order.getReceiverName()).append("</span>");
        html.append("</div>");
        html.append("<div class='info-row'>");
        html.append("<span class='label'>Số điện thoại:</span>");
        html.append("<span>").append(order.getShippingPhone()).append("</span>");
        html.append("</div>");
        html.append("<div class='info-row'>");
        html.append("<span class='label'>Địa chỉ:</span>");
        html.append("<span>").append(order.getShippingAddress()).append("</span>");
        html.append("</div>");
        if (order.getNotes() != null && !order.getNotes().isEmpty()) {
            html.append("<div class='info-row'>");
            html.append("<span class='label'>Ghi chú:</span>");
            html.append("<span>").append(order.getNotes()).append("</span>");
            html.append("</div>");
        }
        html.append("</div>");

        // CTA Button
        html.append("<div style='text-align: center;'>");
        html.append("<a href='http://localhost:8080/account/orders' class='btn'>XEM CHI TIẾT ĐƠN HÀNG</a>");
        html.append("</div>");

        html.append("<p style='margin-top: 30px; font-size: 14px; color: #666;'>");
        html.append("Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi qua hotline hoặc email bên dưới.");
        html.append("</p>");

        html.append("</div>"); // End content

        // Footer
        html.append("<div class='footer'>");
        html.append("<h3>BOIZ SHOP</h3>");
        html.append("<p>📞 Hotline: 1900 xxxx | 📧 Email: support@boizshop.com</p>");
        html.append("<p>📍 Địa chỉ: 123 Đường ABC, Quận XYZ, TP. HCM</p>");
        html.append("<p>🕒 Giờ làm việc: 8:00 - 22:00 (Tất cả các ngày)</p>");
        html.append("<p style='margin-top: 20px; font-size: 12px;'>&copy; 2026 BOIZ SHOP. All rights reserved.</p>");
        html.append("</div>");

        html.append("</div>"); // End container
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    /**
     * Format Order ID: ORD000001
     */
    private String formatOrderId(Integer orderId) {
        return String.format("ORD%06d", orderId);
    }

    /**
     * Format Watch ID: W00001
     */
    private String formatWatchId(Integer watchId) {
        return String.format("W%05d", watchId);
    }

    /**
     * Get status text in Vietnamese
     */
    private String getStatusText(String status) {
        switch (status) {
            case "PENDING": return "Chờ xác nhận";
            case "CONFIRMED": return "Đã xác nhận";
            case "SHIPPING": return "Đang giao hàng";
            case "DELIVERED": return "Đã giao hàng";
            case "CANCELLED": return "Đã hủy";
            default: return status;
        }
    }
}
```

---

## 💻 CODE TÍCH HỢP VÀO OrderService

**File:** `src/main/java/boiz/shop/_2BShop/service/OrderService.java`

Thêm code này vào cuối method `createOrder()`:

```java
// Gửi email xác nhận đơn hàng
try {
    List<OrderDetail> orderDetailsList = orderDetailRepository.findByOrder(order);
    mailService.sendOrderConfirmation(order, orderDetailsList);
} catch (Exception e) {
    // Log lỗi nhưng không throw exception để không ảnh hưởng flow đặt hàng
    System.err.println("Lỗi gửi email xác nhận đơn hàng: " + e.getMessage());
    e.printStackTrace();
}

return order;
```

---

## 🔧 CẤU HÌNH MAIL (application.properties)

Đảm bảo đã cấu hình SMTP trong `application.properties`:

```properties
# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.ssl.trust=smtp.gmail.com

# Custom mail properties
app.mail.title-welcome=Chào mừng đến BOIZ SHOP
app.mail.verify-subject=Xác thực tài khoản BOIZ SHOP
```

### **Lấy App Password Gmail:**
1. Vào Google Account → Security
2. Bật 2-Step Verification
3. Tạo App Password cho "Mail"
4. Copy password vào `spring.mail.password`

---

## 📧 EMAIL SUBJECT

```
[BOIZ SHOP] Xác nhận đơn hàng #ORD000001
```

Format: `[BOIZ SHOP] Xác nhận đơn hàng #[MÃ ĐƠN]`

---

## 🧪 TEST CHỨC NĂNG

### **Test 1: Đặt hàng thành công**
1. Thêm sản phẩm vào giỏ hàng
2. Checkout và điền thông tin
3. Submit đơn hàng
4. ✅ Kiểm tra email đã nhận được
5. ✅ Verify thông tin đầy đủ trong email

### **Test 2: Đơn hàng có nhiều sản phẩm**
1. Thêm 3-5 sản phẩm khác nhau
2. Đặt hàng
3. ✅ Kiểm tra table products hiển thị đầy đủ

### **Test 3: Sản phẩm có giảm giá**
1. Đặt hàng sản phẩm có discount > 0
2. ✅ Kiểm tra hiển thị giá gốc và giá giảm
3. ✅ Verify tính toán đúng

### **Test 4: Miễn phí ship**
1. Đặt hàng > 500,000₫
2. ✅ Kiểm tra hiển thị "Miễn phí"

### **Test 5: Email error handling**
1. Tạm thời tắt mail server
2. Đặt hàng
3. ✅ Đơn hàng vẫn tạo thành công (không bị crash)
4. ✅ Log lỗi trong console

---

## 📊 DỮ LIỆU MẪU EMAIL

### **Order Info:**
```
Mã đơn hàng: ORD000123
Ngày đặt: 11/01/2026 14:30
Trạng thái: Đang xử lý
Phương thức: COD (Thanh toán khi nhận hàng)
```

### **Product Example:**
```
┌─────────────────────────────────────────────┬────────┬───────────┬─────────────┐
│ Sản phẩm                                     │ SL     │ Đơn giá   │ Thành tiền  │
├─────────────────────────────────────────────┼────────┼───────────┼─────────────┤
│ Rolex Submariner Date                        │   1    │ 25,000,000│ 21,250,000₫ │
│ Mã SP: W00001                                │        │ -3,750,000│             │
│ Thương hiệu: Rolex                           │        │           │             │
│ Mô tả: Đồng hồ lặn cao cấp...                │        │           │             │
└─────────────────────────────────────────────┴────────┴───────────┴─────────────┘
```

### **Summary:**
```
Tạm tính:           21,250,000₫
Phí vận chuyển:     Miễn phí
─────────────────────────────────
TỔNG CỘNG:          21,250,000₫
```

---

## ⚡ PERFORMANCE & ERROR HANDLING

### **1. Try-Catch Wrapper**
- Email gửi trong try-catch block
- Nếu lỗi: log error nhưng không throw
- Đảm bảo flow đặt hàng không bị gián đoạn

### **2. Async Email (Optional - Có thể thêm sau)**
```java
@Async
public void sendOrderConfirmationAsync(Order order, List<OrderDetail> orderDetails) {
    sendOrderConfirmation(order, orderDetails);
}
```

---

## 🎁 TÍNH NĂNG BỔ SUNG (Có thể mở rộng)

### **1. Thêm ảnh sản phẩm**
Thay vì chỉ text, hiển thị ảnh thật:
```java
String imageUrl = watch.getImages() != null && !watch.getImages().isEmpty() 
    ? watch.getImages().get(0).getImageUrl() 
    : "default-image.jpg";
productsHtml.append("<img src='http://localhost:8080" + imageUrl + "' style='width:80px;height:80px;'>");
```

### **2. Tracking Link**
```html
<a href='http://localhost:8080/track-order?code=ORD000001'>
    Theo dõi đơn hàng
</a>
```

### **3. QR Code đơn hàng**
Tích hợp thư viện tạo QR code chứa mã đơn hàng

### **4. Email theo trạng thái**
- Email xác nhận (CONFIRMED)
- Email đang giao (SHIPPING)
- Email hoàn thành (DELIVERED)
- Email hủy đơn (CANCELLED)

---

## 🐛 TROUBLESHOOTING

### **Lỗi: Email không gửi được**
**Nguyên nhân:**
- SMTP config sai
- Gmail chặn "Less secure apps"
- Chưa tạo App Password

**Giải pháp:**
1. Kiểm tra config trong application.properties
2. Sử dụng App Password thay vì password thật
3. Check log: `System.err.println("Lỗi gửi email: " + e.getMessage());`

### **Lỗi: NullPointerException**
**Nguyên nhân:**
- Order chưa có orderDetails
- Watch không có brand

**Giải pháp:**
- Kiểm tra orderDetails không null
- Add null checks cho các fields

### **Lỗi: Email format bị lỗi**
**Nguyên nhân:**
- HTML syntax error
- Missing closing tags

**Giải pháp:**
- Validate HTML với online tools
- Test với email client khác nhau

---

## ✅ HOÀN THÀNH!

Email xác nhận đơn hàng đã được tích hợp đầy đủ với:
- ✅ Design đẹp mắt, chuyên nghiệp
- ✅ Thông tin đầy đủ, chi tiết
- ✅ Responsive (hiển thị tốt trên mobile)
- ✅ Error handling an toàn
- ✅ Easy to customize

**🎊 Khách hàng sẽ nhận được email ngay sau khi đặt hàng thành công! 🎊**
