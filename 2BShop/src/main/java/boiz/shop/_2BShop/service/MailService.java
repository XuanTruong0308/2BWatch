package boiz.shop._2BShop.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    @Autowired
    JavaMailSender sender;

    // 1. Lấy email gửi đi từ properties
    @Value("${spring.mail.username}")
    private String fromEmail;

    // 2. Lấy tiêu đề chào mừng từ properties
    @Value("${app.mail.title-welcome}")
    private String welcomeTitle;

    // 3. Lấy tiêu đề xác thực từ properties
    @Value("${app.mail.verify-subject}")
    private String verifyTitle;

    /**
     * Hàm gửi mail tổng quát
     */
    public void send(String to, String subject, String body) {
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true = cho phép nội dung HTML
            helper.setFrom(fromEmail); // Sử dụng email đã cấu hình trong properties

            sender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi gửi mail: " + e.getMessage());
        }
    }

    /**
     * Hàm tiện ích: Gửi mail chào mừng (Dùng title từ properties)
     */
    public void sendWelcome(String to, String fullname) {
        String body = "<h3>Chào mừng " + fullname + "!</h3><p>Cảm ơn bạn đã tham gia BOIZ SHOP.</p>";
        this.send(to, welcomeTitle, body);
    }

    /**
     * Hàm tiện ích: Gửi mã xác thực (Dùng title từ properties)
     */
    public void sendOTP(String to, String token) {
        String body = "<h3>Mã xác thực của bạn là: <b style='color:red'>" + token + "</b></h3>";
        this.send(to, verifyTitle, body);
    }

    /**
     * Gửi email xác thực đăng ký với link confirm
     */
    public void sendRegistrationConfirmation(String to, String fullname, String token) {
        String confirmLink = "http://localhost:8080/confirm-register?token=" + token;

        String body = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: 'Inter', Arial, sans-serif; background: #f8f8f8; padding: 20px; }" +
                ".container { max-width: 600px; margin: 0 auto; background: #ffffff; border: 2px solid #000; padding: 40px; }"
                +
                ".header { text-align: center; border-bottom: 2px solid #000; padding-bottom: 20px; margin-bottom: 30px; }"
                +
                ".title { font-size: 24px; font-weight: 700; letter-spacing: 6px; margin: 0; }" +
                ".content { line-height: 1.8; color: #333; }" +
                ".btn { display: inline-block; padding: 14px 40px; background: #000; color: #fff; text-decoration: none; letter-spacing: 2px; font-size: 12px; font-weight: 600; margin: 20px 0; }"
                +
                ".footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #e0e0e0; text-align: center; font-size: 12px; color: #666; }"
                +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1 class='title'>BOIZ SHOP</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Xin chào <strong>" + fullname + "</strong>,</p>" +
                "<p>Cảm ơn bạn đã đăng ký tài khoản tại BOIZ SHOP - Cửa hàng đồng hồ đẳng cấp quốc tế.</p>" +
                "<p>Để hoàn tất quá trình đăng ký và kích hoạt tài khoản, vui lòng nhấn vào nút bên dưới:</p>" +
                "<center>" +
                "<a href='" + confirmLink + "' class='btn'>XÁC THỰC TÀI KHOẢN</a>" +
                "</center>" +
                "<p>Hoặc copy link sau vào trình duyệt:</p>" +
                "<p style='background: #f8f8f8; padding: 10px; word-break: break-all;'>" + confirmLink + "</p>" +
                "<p><strong>Lưu ý:</strong> Link xác thực này có hiệu lực trong vòng 24 giờ.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>&copy; 2026 BOIZ SHOP. All Rights Reserved.</p>" +
                "<p>Hotline: 1900 8888 | Email: contact@boizshop.vn</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        this.send(to, "[BOIZ SHOP] Xác thực tài khoản đăng ký", body);
    }

    /**
     * Gửi email khôi phục mật khẩu
     */
    public void sendPasswordResetEmail(String to, String fullname, String token) {
        String resetLink = "http://localhost:8080/reset-password?token=" + token;

        String body = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: 'Inter', Arial, sans-serif; background: #f8f8f8; padding: 20px; }" +
                ".container { max-width: 600px; margin: 0 auto; background: #ffffff; border: 2px solid #000; padding: 40px; }"
                +
                ".header { text-align: center; border-bottom: 2px solid #000; padding-bottom: 20px; margin-bottom: 30px; }"
                +
                ".title { font-size: 24px; font-weight: 700; letter-spacing: 6px; margin: 0; }" +
                ".content { line-height: 1.8; color: #333; }" +
                ".btn { display: inline-block; padding: 14px 40px; background: #000; color: #fff; text-decoration: none; letter-spacing: 2px; font-size: 12px; font-weight: 600; margin: 20px 0; }"
                +
                ".warning { background: #fff3cd; border: 1px solid #ffc107; padding: 15px; margin: 20px 0; border-radius: 4px; }"
                +
                ".footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #e0e0e0; text-align: center; font-size: 12px; color: #666; }"
                +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1 class='title'>BOIZ SHOP</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Xin chào <strong>" + fullname + "</strong>,</p>" +
                "<p>Chúng tôi nhận được yêu cầu khôi phục mật khẩu cho tài khoản của bạn tại BOIZ SHOP.</p>" +
                "<p>Để tạo mật khẩu mới, vui lòng nhấn vào nút bên dưới:</p>" +
                "<center>" +
                "<a href='" + resetLink + "' class='btn'>KHÔI PHỤC MẬT KHẨU</a>" +
                "</center>" +
                "<p>Hoặc copy link sau vào trình duyệt:</p>" +
                "<p style='background: #f8f8f8; padding: 10px; word-break: break-all;'>" + resetLink + "</p>" +
                "<div class='warning'>" +
                "<p style='margin: 0;'><strong>⚠️ Lưu ý:</strong></p>" +
                "<p style='margin: 5px 0 0 0;'>Link khôi phục này có hiệu lực trong vòng <strong>2 giờ</strong>.</p>" +
                "<p style='margin: 5px 0 0 0;'>Nếu bạn không yêu cầu khôi phục mật khẩu, vui lòng bỏ qua email này.</p>"
                +
                "</div>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>&copy; 2026 BOIZ SHOP. All Rights Reserved.</p>" +
                "<p>Hotline: 1900 8888 | Email: contact@boizshop.vn</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        this.send(to, "[BOIZ SHOP] Khôi phục mật khẩu", body);
    }

    /**
     * Gửi email liên hệ từ khách hàng
     */
    public void sendContactEmail(String name, String email, String subject, String message) {
        String body = "<h3>Thông tin liên hệ mới từ website:</h3>" +
                "<p><strong>Họ tên:</strong> " + name + "</p>" +
                "<p><strong>Email:</strong> " + email + "</p>" +
                "<p><strong>Chủ đề:</strong> " + subject + "</p>" +
                "<p><strong>Nội dung:</strong></p>" +
                "<p>" + message + "</p>";

        // Gửi đến email admin
        this.send("admin@boizshop.vn", "[CONTACT] " + subject, body);
    }

    /**
     * Gửi email xác nhận đơn hàng với hóa đơn chi tiết
     */
    public void sendOrderConfirmation(boiz.shop._2BShop.entity.Order order,
            java.util.List<boiz.shop._2BShop.entity.OrderDetail> orderDetails) {
        String customerName = order.getReceiverName();
        String customerEmail = order.getUser().getEmail();
        String orderCode = "ORD" + String.format("%06d", order.getOrderId());

        // Tính toán
        java.math.BigDecimal subtotal = java.math.BigDecimal.ZERO;
        for (boiz.shop._2BShop.entity.OrderDetail detail : orderDetails) {
            subtotal = subtotal.add(detail.getSubtotal());
        }

        java.math.BigDecimal shippingFee = order.getTotalAmount().subtract(subtotal);

        // Format ngày
        String orderDate = order.getOrderDate()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        // Build HTML email với thiết kế đẹp
        StringBuilder productsHtml = new StringBuilder();
        for (boiz.shop._2BShop.entity.OrderDetail detail : orderDetails) {
            boiz.shop._2BShop.entity.Watch watch = detail.getWatch();
            java.math.BigDecimal finalPrice = detail.getUnitPrice().subtract(detail.getDiscountAmount());
            String watchCode = "W" + String.format("%05d", watch.getWatchId());

            productsHtml.append("<tr style='border-bottom: 1px solid #e0e0e0;'>")
                    .append("<td style='padding: 20px 10px;'>")
                    .append("<div>")
                    .append("<div style='font-weight: 600; color: #1a1a1a; margin-bottom: 5px;'>")
                    .append(watch.getWatchName()).append("</div>")
                    .append("<div style='font-size: 12px; color: #666; margin-bottom: 3px;'>Mã SP: <strong>")
                    .append(watchCode).append("</strong></div>")
                    .append("<div style='font-size: 12px; color: #666;'>Thương hiệu: <strong>")
                    .append(watch.getBrand().getBrandName()).append("</strong></div>");

            if (watch.getDescription() != null && !watch.getDescription().isEmpty()) {
                String shortDesc = watch.getDescription().length() > 80
                        ? watch.getDescription().substring(0, 80) + "..."
                        : watch.getDescription();
                productsHtml.append("<div style='font-size: 11px; color: #999; margin-top: 5px; font-style: italic;'>")
                        .append(shortDesc).append("</div>");
            }

            productsHtml.append("</div></td>")
                    .append("<td style='padding: 20px 10px; text-align: center; color: #333;'>")
                    .append(detail.getQuantity()).append("</td>")
                    .append("<td style='padding: 20px 10px; text-align: right;'>")
                    .append("<div style='color: #666; font-size: 13px;'>")
                    .append(String.format("%,d₫", detail.getUnitPrice().longValue()))
                    .append("</div>");

            if (detail.getDiscountAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
                productsHtml.append("<div style='color: #e74c3c; font-size: 12px; margin-top: 3px;'>")
                        .append("-").append(String.format("%,d₫", detail.getDiscountAmount().longValue()))
                        .append("</div>");
            }

            productsHtml.append("</td>")
                    .append("<td style='padding: 20px 10px; text-align: right; font-weight: 600; color: #1a1a1a;'>")
                    .append(String.format("%,d₫", detail.getSubtotal().longValue()))
                    .append("</td></tr>");
        }

        String body = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f8f9fa; margin: 0; padding: 20px; }"
                +
                ".container { max-width: 700px; margin: 0 auto; background: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }"
                +
                ".header { background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%); color: #ffffff; padding: 30px; text-align: center; }"
                +
                ".header h1 { margin: 0; font-size: 28px; letter-spacing: 4px; font-weight: 700; }" +
                ".header p { margin: 10px 0 0 0; font-size: 14px; opacity: 0.9; }" +
                ".success-badge { background: #27ae60; color: white; padding: 8px 20px; border-radius: 20px; display: inline-block; margin-top: 15px; font-size: 13px; font-weight: 600; }"
                +
                ".content { padding: 30px; }" +
                ".order-info { background: #f8f9fa; border-left: 4px solid #3498db; padding: 20px; margin-bottom: 30px; border-radius: 4px; }"
                +
                ".order-info h3 { margin: 0 0 15px 0; color: #1a1a1a; font-size: 16px; }" +
                ".info-row { display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 14px; }" +
                ".info-label { color: #666; }" +
                ".info-value { color: #1a1a1a; font-weight: 600; }" +
                ".section-title { font-size: 18px; font-weight: 600; color: #1a1a1a; margin: 30px 0 20px 0; border-bottom: 2px solid #1a1a2e; padding-bottom: 10px; }"
                +
                ".products-table { width: 100%; border-collapse: collapse; margin-bottom: 30px; }" +
                ".products-table th { background: #1a1a2e; color: white; padding: 12px 10px; text-align: left; font-size: 13px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; }"
                +
                ".summary { background: #f8f9fa; padding: 20px; border-radius: 4px; margin-top: 20px; }" +
                ".summary-row { display: flex; justify-content: space-between; margin-bottom: 12px; font-size: 14px; }"
                +
                ".summary-row.total { font-size: 18px; font-weight: 700; color: #e74c3c; border-top: 2px solid #ddd; padding-top: 15px; margin-top: 15px; }"
                +
                ".shipping-info { background: #fff3cd; border: 1px solid #ffc107; padding: 20px; border-radius: 4px; margin-top: 30px; }"
                +
                ".shipping-info h4 { margin: 0 0 15px 0; color: #856404; font-size: 15px; }" +
                ".footer { background: #1a1a2e; color: #ffffff; padding: 30px; text-align: center; font-size: 13px; }" +
                ".footer p { margin: 5px 0; opacity: 0.8; }" +
                ".btn { display: inline-block; padding: 12px 30px; background: #3498db; color: #fff; text-decoration: none; border-radius: 4px; margin: 20px 0; font-weight: 600; }"
                +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +

                // Header
                "<div class='header'>" +
                "<h1>BOIZ SHOP</h1>" +
                "<p>Đồng hồ cao cấp - Đẳng cấp quốc tế</p>" +
                "<div class='success-badge'>✓ ĐẶT HÀNG THÀNH CÔNG</div>" +
                "</div>" +

                // Content
                "<div class='content'>" +
                "<p style='font-size: 15px; color: #333; margin-bottom: 20px;'>Xin chào <strong>" + customerName
                + "</strong>,</p>" +
                "<p style='font-size: 14px; color: #666; line-height: 1.6;'>Cảm ơn bạn đã tin tưởng và đặt hàng tại <strong>BOIZ SHOP</strong>.</p>" +
                "<p style='font-size: 14px; color: #666; line-height: 1.6;'>Đơn hàng của bạn đã được <strong style='color: #28a745;'>tiếp nhận thành công</strong>. Nhân viên của chúng tôi sẽ liên hệ trong vòng 24 giờ để xác nhận và sắp xếp giao hàng.</p>"
                +

                // Order Info Box
                "<div class='order-info'>" +
                "<h3>📋 THÔNG TIN ĐỚN HÀNG</h3>" +
                "<div class='info-row'>" +
                "<span class='info-label'>Mã đơn hàng:</span>" +
                "<span class='info-value'>" + orderCode + "</span>" +
                "</div>" +
                "<div class='info-row'>" +
                "<span class='info-label'>Ngày đặt hàng:</span>" +
                "<span class='info-value'>" + orderDate + "</span>" +
                "</div>" +
                "<div class='info-row'>" +
                "<span class='info-label'>Trạng thái:</span>" +
                "<span class='info-value' style='color: #f39c12;'>Đang xử lý</span>" +
                "</div>" +
                "<div class='info-row'>" +
                "<span class='info-label'>Phương thức thanh toán:</span>" +
                "<span class='info-value'>" + order.getPaymentMethod().getMethodName() + "</span>" +
                "</div>" +
                "</div>" +

                // Products Section
                "<div class='section-title'>🛍️ CHI TIẾT SẢN PHẨM</div>" +
                "<table class='products-table'>" +
                "<thead>" +
                "<tr>" +
                "<th>Sản phẩm</th>" +
                "<th style='text-align: center; width: 80px;'>Số lượng</th>" +
                "<th style='text-align: right; width: 120px;'>Đơn giá</th>" +
                "<th style='text-align: right; width: 120px;'>Thành tiền</th>" +
                "</tr>" +
                "</thead>" +
                "<tbody>" +
                productsHtml.toString() +
                "</tbody>" +
                "</table>" +

                // Summary
                "<div class='summary'>" +
                "<div class='summary-row'>" +
                "<span>Tạm tính:</span>" +
                "<span style='font-weight: 600;'>" + String.format("%,d₫", subtotal.longValue()) + "</span>" +
                "</div>" +
                "<div class='summary-row'>" +
                "<span>Phí vận chuyển:</span>" +
                "<span style='font-weight: 600;'>"
                + (shippingFee.compareTo(java.math.BigDecimal.ZERO) == 0 ? "Miễn phí"
                        : String.format("%,d₫", shippingFee.longValue()))
                + "</span>" +
                "</div>" +
                "<div class='summary-row total'>" +
                "<span>TỔNG CỘNG:</span>" +
                "<span>" + String.format("%,d₫", order.getTotalAmount().longValue()) + "</span>" +
                "</div>" +
                "</div>" +

                // Shipping Info
                "<div class='shipping-info'>" +
                "<h4>📦 THÔNG TIN GIAO HÀNG</h4>" +
                "<p style='margin: 5px 0; font-size: 14px; color: #333;'><strong>Người nhận:</strong> "
                + order.getReceiverName() + "</p>" +
                "<p style='margin: 5px 0; font-size: 14px; color: #333;'><strong>Số điện thoại:</strong> "
                + order.getShippingPhone() + "</p>" +
                "<p style='margin: 5px 0; font-size: 14px; color: #333;'><strong>Địa chỉ:</strong> "
                + order.getShippingAddress() + "</p>" +
                (order.getNotes() != null && !order.getNotes().isEmpty()
                        ? "<p style='margin: 5px 0; font-size: 14px; color: #333;'><strong>Ghi chú:</strong> "
                                + order.getNotes() + "</p>"
                        : "")
                +
                "</div>" +

                "<p style='margin-top: 30px; font-size: 14px; color: #666; line-height: 1.8;'>" +
                "Chúng tôi sẽ liên hệ với bạn để xác nhận đơn hàng trong thời gian sớm nhất. " +
                "Nếu có bất kỳ thắc mắc nào, vui lòng liên hệ hotline <strong style='color: #e74c3c;'>1900 8888</strong> để được hỗ trợ."
                +
                "</p>" +

                // Download VAT Invoice section
                "<div style='background: #f8f9fa; border: 2px dashed #d4af37; border-radius: 10px; padding: 20px; margin: 30px 0; text-align: center;'>" +
                "<h4 style='color: #1a365d; margin-bottom: 15px;'>📄 TẢI HÓA ĐƠN VAT</h4>" +
                "<p style='font-size: 14px; color: #666; margin-bottom: 20px;'>Bạn có thể tải hóa đơn VAT của đơn hàng dưới định dạng Word hoặc PDF</p>" +
                "<div style='display: inline-block;'>" +
                "<a href='http://localhost:8080/invoice/" + order.getOrderId() + "/word' style='display: inline-block; padding: 12px 30px; background: #d4af37; color: #fff; text-decoration: none; border-radius: 5px; margin: 0 5px; font-weight: 600; font-size: 14px;'>" +
                "<span style='margin-right: 5px;'>📝</span> Tải Word (.docx)" +
                "</a>" +
                "<a href='http://localhost:8080/invoice/" + order.getOrderId() + "/pdf' style='display: inline-block; padding: 12px 30px; background: #dc3545; color: #fff; text-decoration: none; border-radius: 5px; margin: 0 5px; font-weight: 600; font-size: 14px;'>" +
                "<span style='margin-right: 5px;'>📕</span> Tải PDF (.pdf)" +
                "</a>" +
                "</div>" +
                "</div>" +

                "<center>" +
                "<a href='http://localhost:8080/account/orders' class='btn'>XEM CHI TIẾT ĐƠN HÀNG</a>" +
                "</center>" +

                "</div>" +

                // Footer
                "<div class='footer'>" +
                "<p style='font-weight: 600; font-size: 14px; margin-bottom: 10px;'>BOIZ SHOP - ĐỒNG HỒ CAO CẤP</p>" +
                "<p>📍 123 Đường ABC, Quận 1, TP. Hồ Chí Minh</p>" +
                "<p>📞 Hotline: 1900 8888 | ✉️ Email: contact@boizshop.vn</p>" +
                "<p>⏰ Thời gian làm việc: 8:00 - 22:00 (Thứ 2 - Chủ nhật)</p>" +
                "<p style='margin-top: 15px;'>&copy; 2026 BOIZ SHOP. All Rights Reserved.</p>" +
                "</div>" +

                "</div>" +
                "</body>" +
                "</html>";

        this.send(customerEmail, "[BOIZ SHOP] Xác nhận đơn hàng #" + orderCode, body);
    }

    /**
     * Gửi email thông báo đơn hàng đang vận chuyển
     */
    public void sendShippingEmail(String to, String name, String orderCode) {
        String subject = "[BOIZ SHOP] Đơn hàng #" + orderCode + " đang được giao";
        String body = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: 'Inter', Arial, sans-serif; background: #f8f8f8; padding: 20px; }" +
                ".container { max-width: 600px; margin: 0 auto; background: #ffffff; border: 2px solid #000; padding: 40px; }"
                +
                ".header { text-align: center; border-bottom: 2px solid #000; padding-bottom: 20px; margin-bottom: 30px; }"
                +
                ".title { font-size: 24px; font-weight: 700; letter-spacing: 6px; margin: 0; }" +
                ".content { line-height: 1.8; color: #333; }" +
                ".btn { display: inline-block; padding: 14px 40px; background: #000; color: #fff; text-decoration: none; letter-spacing: 2px; font-size: 12px; font-weight: 600; margin: 20px 0; }"
                +
                ".footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #e0e0e0; text-align: center; font-size: 12px; color: #666; }"
                +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1 class='title'>BOIZ SHOP</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Xin chào <strong>" + name + "</strong>,</p>" +
                "<p>Đơn hàng <strong>#" + orderCode
                + "</strong> của bạn đã được bàn giao cho đơn vị vận chuyển và đang trên đường đến với bạn.</p>" +
                "<p>Vui lòng chú ý điện thoại để nhận hàng.</p>" +
                "<center><a href='http://localhost:8080/user/orders' class='btn'>XEM ĐƠN HÀNG</a></center>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>&copy; 2026 BOIZ SHOP. All Rights Reserved.</p>" +
                "<p>Hotline: 1900 8888 | Email: contact@boizshop.vn</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
        this.send(to, subject, body);
    }

    /**
     * Gửi email thông báo giao hàng thành công
     */
    public void sendDeliveredEmail(String to, String name, String orderCode) {
        String subject = "[BOIZ SHOP] Đơn hàng #" + orderCode + " đã giao thành công";
        String body = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: 'Inter', Arial, sans-serif; background: #f8f8f8; padding: 20px; }" +
                ".container { max-width: 600px; margin: 0 auto; background: #ffffff; border: 2px solid #000; padding: 40px; }"
                +
                ".header { text-align: center; border-bottom: 2px solid #000; padding-bottom: 20px; margin-bottom: 30px; }"
                +
                ".title { font-size: 24px; font-weight: 700; letter-spacing: 6px; margin: 0; }" +
                ".content { line-height: 1.8; color: #333; }" +
                ".btn { display: inline-block; padding: 14px 40px; background: #000; color: #fff; text-decoration: none; letter-spacing: 2px; font-size: 12px; font-weight: 600; margin: 20px 0; }"
                +
                ".footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #e0e0e0; text-align: center; font-size: 12px; color: #666; }"
                +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1 class='title'>BOIZ SHOP</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Xin chào <strong>" + name + "</strong>,</p>" +
                "<p>Đơn hàng <strong>#" + orderCode
                + "</strong> đã được giao thành công. Cảm ơn bạn đã tin tưởng và mua sắm tại BOIZ SHOP.</p>" +
                "<p>Rất mong được phục vụ bạn trong những lần mua sắm tiếp theo!</p>" +
                "<center><a href='http://localhost:8080/user/orders' class='btn'>ĐÁNH GIÁ SẢN PHẨM</a></center>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>&copy; 2026 BOIZ SHOP. All Rights Reserved.</p>" +
                "<p>Hotline: 1900 8888 | Email: contact@boizshop.vn</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
        this.send(to, subject, body);
    }

    /**
     * Gửi email thông báo hoàn thành đơn hàng
     */
    public void sendCompletedEmail(String to, String name, String orderCode) {
        String subject = "[BOIZ SHOP] Cảm ơn bạn đã mua hàng - Đơn #" + orderCode;
        String body = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: 'Inter', Arial, sans-serif; background: #f8f8f8; padding: 20px; }" +
                ".container { max-width: 600px; margin: 0 auto; background: #ffffff; border: 2px solid #000; padding: 40px; }"
                +
                ".header { text-align: center; border-bottom: 2px solid #000; padding-bottom: 20px; margin-bottom: 30px; }"
                +
                ".title { font-size: 24px; font-weight: 700; letter-spacing: 6px; margin: 0; }" +
                ".content { line-height: 1.8; color: #333; }" +
                ".btn { display: inline-block; padding: 14px 40px; background: #000; color: #fff; text-decoration: none; letter-spacing: 2px; font-size: 12px; font-weight: 600; margin: 20px 0; }"
                +
                ".footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #e0e0e0; text-align: center; font-size: 12px; color: #666; }"
                +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1 class='title'>BOIZ SHOP</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Xin chào <strong>" + name + "</strong>,</p>" +
                "<p>Đơn hàng <strong>#" + orderCode + "</strong> của bạn đã hoàn tất.</p>" +
                "<p>Chúng tôi hy vọng bạn hài lòng với sản phẩm và dịch vụ của BOIZ SHOP.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>&copy; 2026 BOIZ SHOP. All Rights Reserved.</p>" +
                "<p>Hotline: 1900 8888 | Email: contact@boizshop.vn</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
        this.send(to, subject, body);
    }

    /**
     * Gửi email thông báo hủy đơn hàng
     */
    public void sendCancelledEmail(String to, String name, String orderCode) {
        String subject = "[BOIZ SHOP] Đơn hàng #" + orderCode + " đã bị hủy";
        String body = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: 'Inter', Arial, sans-serif; background: #f8f8f8; padding: 20px; }" +
                ".container { max-width: 600px; margin: 0 auto; background: #ffffff; border: 2px solid #000; padding: 40px; }"
                +
                ".header { text-align: center; border-bottom: 2px solid #000; padding-bottom: 20px; margin-bottom: 30px; }"
                +
                ".title { font-size: 24px; font-weight: 700; letter-spacing: 6px; margin: 0; color: #d32f2f; }" +
                ".content { line-height: 1.8; color: #333; }" +
                ".btn { display: inline-block; padding: 14px 40px; background: #000; color: #fff; text-decoration: none; letter-spacing: 2px; font-size: 12px; font-weight: 600; margin: 20px 0; }"
                +
                ".footer { margin-top: 30px; padding-top: 20px; border-top: 1px solid #e0e0e0; text-align: center; font-size: 12px; color: #666; }"
                +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1 class='title'>ĐƠN HÀNG ĐÃ HỦY</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Xin chào <strong>" + name + "</strong>,</p>" +
                "<p>Đơn hàng <strong>#" + orderCode + "</strong> đã bị hủy theo yêu cầu.</p>" +
                "<p>Nếu bạn đã thanh toán, chúng tôi sẽ tiến hành hoàn tiền trong thời gian sớm nhất (3-5 ngày làm việc).</p>"
                +
                "<p>Nếu bạn có thắc mắc hoặc cần hỗ trợ thêm, vui lòng liên hệ với chúng tôi.</p>" +
                "<center><a href='http://localhost:8080/' class='btn'>TIẾP TỤC MUA SẮM</a></center>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>&copy; 2026 BOIZ SHOP. All Rights Reserved.</p>" +
                "<p>Hotline: 1900 8888 | Email: contact@boizshop.vn</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
        this.send(to, subject, body);
    }
}