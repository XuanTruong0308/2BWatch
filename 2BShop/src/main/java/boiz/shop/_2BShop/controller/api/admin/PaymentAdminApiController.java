package boiz.shop._2BShop.controller.api.admin;

import boiz.shop._2BShop.controller.api.ApiDataMapper;
import boiz.shop._2BShop.entity.PaymentMethod;
import boiz.shop._2BShop.entity.PaymentTransaction;
import boiz.shop._2BShop.respository.PaymentMethodRepository;
import boiz.shop._2BShop.respository.PaymentTransactionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Admin Payment API")
@RestController
@RequestMapping("/api/v1/admin/payments")
public class PaymentAdminApiController {

    @Autowired
    private PaymentMethodRepository paymentMethodRepo;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepo;

    @Operation(summary = "Get payment methods")
    @GetMapping("/methods")
    public ResponseEntity<Map<String, Object>> methods() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<PaymentMethod> methods = paymentMethodRepo.findAll();
            response.put("success", true);
            response.put("data", methods.stream().map(ApiDataMapper::paymentMethod).toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Get payment method detail")
    @GetMapping("/methods/{id}")
    public ResponseEntity<Map<String, Object>> methodDetail(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            PaymentMethod method = paymentMethodRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Payment method not found with id: " + id));
            response.put("success", true);
            response.put("data", ApiDataMapper.paymentMethod(method));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Save payment method")
    @PostMapping("/methods")
    public ResponseEntity<Map<String, Object>> saveMethod(@RequestBody PaymentMethod paymentMethod) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (paymentMethod.getPaymentMethodId() == null) {
                paymentMethod.setCreatedDate(LocalDateTime.now());
            }
            paymentMethod.setUpdatedDate(LocalDateTime.now());

            PaymentMethod saved = paymentMethodRepo.save(paymentMethod);
            response.put("success", true);
            response.put("message", "Lưu phương thức thanh toán thành công!");
            response.put("data", ApiDataMapper.paymentMethod(saved));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Toggle payment method active status")
    @PatchMapping("/methods/{id}/toggle-active")
    public ResponseEntity<Map<String, Object>> toggleMethod(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            PaymentMethod method = paymentMethodRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Payment method not found"));

            method.setIsActive(!method.getIsActive());
            method.setUpdatedDate(LocalDateTime.now());
            paymentMethodRepo.save(method);

            response.put("success", true);
            response.put("message", "Đã chuyển phương thức thanh toán sang " + (method.getIsActive() ? "Active" : "Inactive"));
            response.put("data", ApiDataMapper.paymentMethod(method));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Get payment transactions")
    @GetMapping("/transactions")
    public ResponseEntity<Map<String, Object>> transactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer methodId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
            Page<PaymentTransaction> transactions;

            if (status != null && !status.isEmpty()) {
                transactions = paymentTransactionRepo.findByStatus(status, pageable);
            } else if (methodId != null) {
                transactions = paymentTransactionRepo.findByPaymentMethodPaymentMethodId(methodId, pageable);
            } else {
                transactions = paymentTransactionRepo.findAll(pageable);
            }

            BigDecimal totalAmount = transactions.getContent().stream()
                    .map(PaymentTransaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> data = new HashMap<>();
            data.put("items", ApiDataMapper.mapPaymentTransactions(transactions.getContent()));
            data.put("page", ApiDataMapper.pageInfo(transactions));
            data.put("totalAmount", totalAmount);
            data.put("paymentMethods", paymentMethodRepo.findAll().stream().map(ApiDataMapper::paymentMethod).toList());

            Map<String, Object> filters = new HashMap<>();
            filters.put("status", status);
            filters.put("methodId", methodId);
            data.put("filters", filters);

            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Get payment transaction detail")
    @GetMapping("/transactions/{id}")
    public ResponseEntity<Map<String, Object>> transactionDetail(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            PaymentTransaction transaction = paymentTransactionRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
            response.put("success", true);
            response.put("data", ApiDataMapper.paymentTransaction(transaction));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
