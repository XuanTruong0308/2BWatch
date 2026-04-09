package boiz.shop._2BShop.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriComponentsBuilder;

import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.entity.PaymentTransaction;
import boiz.shop._2BShop.respository.PaymentTransactionRepository;
import boiz.shop._2BShop.service.OrderService;
import boiz.shop._2BShop.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private VNPayService vnPayService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @GetMapping("/vnpay-return")
    public String vnpayReturn(HttpServletRequest request) {
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
            return buildRedirect(
                    false,
                    "Chữ ký không hợp lệ. Giao dịch có thể bị giả mạo!",
                    null,
                    null,
                    null,
                    null,
                    null,
                    null);
        }

        String responseCode = params.get("vnp_ResponseCode");
        String txnRef = params.get("vnp_TxnRef");
        String transactionNo = params.get("vnp_TransactionNo");
        String amount = params.get("vnp_Amount");
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
            order.setOrderStatus("CONFIRMED");
            order.setUpdatedDate(LocalDateTime.now());
            orderService.updateStatus(orderId, "CONFIRMED", "Thanh toán VNPay thành công");
            return buildRedirect(
                    true,
                    "Thanh toán thành công!",
                    orderId,
                    "ORD" + String.format("%06d", orderId),
                    amount != null ? String.valueOf(Long.parseLong(amount) / 100) : null,
                    transactionNo,
                    bankCode,
                    payDate);
        }

        return buildRedirect(
                false,
                getResponseMessage(responseCode),
                orderId,
                "ORD" + String.format("%06d", orderId),
                null,
                transactionNo,
                bankCode,
                payDate);
    }

    private String buildRedirect(
            boolean success,
            String message,
            Integer orderId,
            String orderCode,
            String amount,
            String transactionCode,
            String bankCode,
            String payDate) {
        String targetUrl = UriComponentsBuilder.fromPath("/payment-result")
                .queryParam("success", success)
                .queryParam("message", message)
                .queryParamIfPresent("orderId", Optional.ofNullable(orderId))
                .queryParamIfPresent("orderCode", Optional.ofNullable(orderCode))
                .queryParamIfPresent("amount", Optional.ofNullable(amount))
                .queryParamIfPresent("transactionCode", Optional.ofNullable(transactionCode))
                .queryParamIfPresent("bankCode", Optional.ofNullable(bankCode))
                .queryParamIfPresent("payDate", Optional.ofNullable(payDate))
                .build()
                .encode()
                .toUriString();
        return "redirect:" + targetUrl;
    }

    private String getResponseMessage(String responseCode) {
        return switch (responseCode) {
            case "00" -> "Giao dịch thành công";
            case "07" -> "Trừ tiền thành công. Giao dịch bị nghi ngờ.";
            case "09" -> "Thẻ hoặc tài khoản chưa đăng ký Internet Banking.";
            case "10" -> "Xác thực thông tin thẻ hoặc tài khoản không đúng quá 3 lần.";
            case "11" -> "Đã hết hạn chờ thanh toán.";
            case "12" -> "Thẻ hoặc tài khoản của khách hàng bị khóa.";
            case "13" -> "Sai mật khẩu xác thực giao dịch (OTP).";
            case "24" -> "Khách hàng đã hủy giao dịch.";
            case "51" -> "Tài khoản không đủ số dư để thực hiện giao dịch.";
            case "65" -> "Tài khoản đã vượt hạn mức giao dịch trong ngày.";
            case "75" -> "Ngân hàng thanh toán đang bảo trì.";
            case "79" -> "Nhập sai mật khẩu thanh toán quá số lần quy định.";
            default -> "Giao dịch thất bại. Mã lỗi: " + responseCode;
        };
    }
}
