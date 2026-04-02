package boiz.shop._2BShop.controller.api;

import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.entity.PaymentTransaction;
import boiz.shop._2BShop.respository.PaymentTransactionRepository;
import boiz.shop._2BShop.service.OrderService;
import boiz.shop._2BShop.service.VNPayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Payment API")
@RestController
@RequestMapping("/api/v1/payments")
public class PaymentApiController {

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Operation(summary = "VNPay return callback")
    @GetMapping("/vnpay-return")
    public ResponseEntity<Map<String, Object>> vnpayReturn(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();

            for (String key : requestParams.keySet()) {
                String[] values = requestParams.get(key);
                if (values != null && values.length > 0) {
                    params.put(key, values[0]);
                }
            }

            boolean isValid = vnPayService.verifyPayment(params);
            if (!isValid) {
                response.put("success", false);
                response.put("message", "Chữ ký không hợp lệ. Giao dịch có thể bị giả mạo!");
                return ResponseEntity.badRequest().body(response);
            }

            String responseCode = params.get("vnp_ResponseCode");
            String txnRef = params.get("vnp_TxnRef");
            String transactionNo = params.get("vnp_TransactionNo");
            String amount = params.get("vnp_Amount");
            String orderInfo = params.get("vnp_OrderInfo");
            String bankCode = params.get("vnp_BankCode");
            String payDate = params.get("vnp_PayDate");

            Integer orderId = Integer.parseInt(txnRef);
            Order order = orderService.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

            PaymentTransaction transaction = paymentTransactionRepository
                    .findByOrderOrderId(orderId)
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (transaction != null) {
                transaction.setTransactionCode(transactionNo);
                transaction.setStatus("00".equals(responseCode) ? "SUCCESS" : "FAILED");
                transaction.setResponseData(params.toString());
                transaction.setTransactionDate(LocalDateTime.now());
                paymentTransactionRepository.save(transaction);
            }

            if ("00".equals(responseCode)) {
                orderService.updateStatus(orderId, "CONFIRMED", "Thanh toán VNPay thành công");

                response.put("success", true);
                response.put("message", "Thanh toán thành công!");
                response.put("orderId", orderId);
                response.put("orderCode", "ORD" + String.format("%06d", orderId));
                response.put("amount", Long.parseLong(amount) / 100);
                response.put("transactionCode", transactionNo);
                response.put("bankCode", bankCode);
                response.put("payDate", payDate);
                response.put("orderInfo", orderInfo);
                response.put("orderStatus", order.getOrderStatus());
                return ResponseEntity.ok(response);
            }

            response.put("success", false);
            response.put("message", getResponseMessage(responseCode));
            response.put("orderId", orderId);
            response.put("orderCode", "ORD" + String.format("%06d", orderId));
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

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
