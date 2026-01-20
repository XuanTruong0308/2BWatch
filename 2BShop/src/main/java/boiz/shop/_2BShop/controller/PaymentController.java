package boiz.shop._2BShop.controller;

import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.entity.PaymentTransaction;
import boiz.shop._2BShop.respository.PaymentTransactionRepository;
import boiz.shop._2BShop.service.OrderService;
import boiz.shop._2BShop.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller xử lý thanh toán
 */
@Controller
@RequestMapping("/payment")
public class PaymentController {
    
    @Autowired
    private VNPayService vnPayService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;
    
    /**
     * VNPay return URL - Callback sau khi thanh toán
     */
    @GetMapping("/vnpay-return")
    public String vnpayReturn(HttpServletRequest request, Model model) {
        // Lấy tất cả parameters từ VNPay
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        
        for (String key : requestParams.keySet()) {
            String[] values = requestParams.get(key);
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        }
        
        // Verify signature
        boolean isValid = vnPayService.verifyPayment(params);
        
        if (!isValid) {
            model.addAttribute("success", false);
            model.addAttribute("message", "Chữ ký không hợp lệ. Giao dịch có thể bị giả mạo!");
            return "payment-result";
        }
        
        // Lấy thông tin từ VNPay
        String vnp_ResponseCode = params.get("vnp_ResponseCode");
        String vnp_TxnRef = params.get("vnp_TxnRef"); // Order ID
        String vnp_TransactionNo = params.get("vnp_TransactionNo"); // VNPay Transaction Code
        String vnp_Amount = params.get("vnp_Amount");
        String vnp_OrderInfo = params.get("vnp_OrderInfo");
        String vnp_BankCode = params.get("vnp_BankCode");
        String vnp_PayDate = params.get("vnp_PayDate");
        
        // Parse order ID
        Integer orderId = Integer.parseInt(vnp_TxnRef);
        
        // Tìm order
        Order order = orderService.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        
        // Tìm payment transaction
        PaymentTransaction transaction = paymentTransactionRepository
            .findByOrderOrderId(orderId)
            .stream()
            .findFirst()
            .orElse(null);
        
        if (transaction != null) {
            // Update transaction
            transaction.setTransactionCode(vnp_TransactionNo);
            transaction.setStatus(vnp_ResponseCode.equals("00") ? "SUCCESS" : "FAILED");
            transaction.setResponseData(params.toString());
            transaction.setTransactionDate(LocalDateTime.now());
            paymentTransactionRepository.save(transaction);
        }
        
        // Check response code
        if ("00".equals(vnp_ResponseCode)) {
            // Thanh toán thành công
            order.setOrderStatus("CONFIRMED");
            order.setUpdatedDate(LocalDateTime.now());
            orderService.updateStatus(orderId, "CONFIRMED", "Thanh toán VNPay thành công");
            
            model.addAttribute("success", true);
            model.addAttribute("message", "Thanh toán thành công!");
            model.addAttribute("orderId", orderId);
            model.addAttribute("orderCode", "ORD" + String.format("%06d", orderId));
            model.addAttribute("amount", Long.parseLong(vnp_Amount) / 100);
            model.addAttribute("transactionCode", vnp_TransactionNo);
            model.addAttribute("bankCode", vnp_BankCode);
            model.addAttribute("payDate", vnp_PayDate);
            
        } else {
            // Thanh toán thất bại
            model.addAttribute("success", false);
            model.addAttribute("message", getResponseMessage(vnp_ResponseCode));
            model.addAttribute("orderId", orderId);
            model.addAttribute("orderCode", "ORD" + String.format("%06d", orderId));
        }
        
        return "payment-result";
    }
    
    /**
     * Mapping response code sang message
     */
    private String getResponseMessage(String responseCode) {
        return switch (responseCode) {
            case "00" -> "Giao dịch thành công";
            case "07" -> "Trừ tiền thành công. Giao dịch bị nghi ngờ (liên quan tới lừa đảo, giao dịch bất thường).";
            case "09" -> "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng chưa đăng ký dịch vụ InternetBanking tại ngân hàng.";
            case "10" -> "Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11" -> "Giao dịch không thành công do: Đã hết hạn chờ thanh toán. Xin quý khách vui lòng thực hiện lại giao dịch.";
            case "12" -> "Giao dịch không thành công do: Thẻ/Tài khoản của khách hàng bị khóa.";
            case "13" -> "Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch (OTP). Xin quý khách vui lòng thực hiện lại giao dịch.";
            case "24" -> "Giao dịch không thành công do: Khách hàng hủy giao dịch";
            case "51" -> "Giao dịch không thành công do: Tài khoản của quý khách không đủ số dư để thực hiện giao dịch.";
            case "65" -> "Giao dịch không thành công do: Tài khoản của Quý khách đã vượt quá hạn mức giao dịch trong ngày.";
            case "75" -> "Ngân hàng thanh toán đang bảo trì.";
            case "79" -> "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định. Xin quý khách vui lòng thực hiện lại giao dịch";
            default -> "Giao dịch thất bại. Mã lỗi: " + responseCode;
        };
    }
}
