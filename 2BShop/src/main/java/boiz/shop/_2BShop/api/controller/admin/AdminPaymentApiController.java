package boiz.shop._2BShop.api.controller.admin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import boiz.shop._2BShop.api.mapper.ApiMapper;
import boiz.shop._2BShop.api.request.PaymentMethodUpsertRequest;
import boiz.shop._2BShop.api.response.ApiResponse;
import boiz.shop._2BShop.api.response.PaginatedResponse;
import boiz.shop._2BShop.entity.PaymentMethod;
import boiz.shop._2BShop.entity.PaymentTransaction;
import boiz.shop._2BShop.respository.PaymentMethodRepository;
import boiz.shop._2BShop.respository.PaymentTransactionRepository;

@RestController
@RequestMapping("/api/v1/admin/payments")
public class AdminPaymentApiController {

    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final ApiMapper apiMapper;

    public AdminPaymentApiController(
            PaymentMethodRepository paymentMethodRepository,
            PaymentTransactionRepository paymentTransactionRepository,
            ApiMapper apiMapper) {
        this.paymentMethodRepository = paymentMethodRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.apiMapper = apiMapper;
    }

    @GetMapping("/methods")
    public ApiResponse<List<?>> methods() {
        return ApiResponse.success(paymentMethodRepository.findAll().stream().map(apiMapper::toPaymentMethodDto).toList());
    }

    @GetMapping("/methods/{id}")
    public ApiResponse<?> methodDetail(@PathVariable Integer id) {
        return ApiResponse.success(apiMapper.toPaymentMethodDto(findMethod(id)));
    }

    @PostMapping("/methods")
    public ApiResponse<?> createMethod(@RequestBody PaymentMethodUpsertRequest request) {
        PaymentMethod method = new PaymentMethod();
        method.setMethodName(request.methodName());
        method.setDescription(request.description());
        method.setIsActive(request.active() == null ? true : request.active());
        method.setCreatedDate(LocalDateTime.now());
        method.setUpdatedDate(LocalDateTime.now());
        return ApiResponse.success("Lưu phương thức thanh toán thành công", apiMapper.toPaymentMethodDto(paymentMethodRepository.save(method)));
    }

    @PutMapping("/methods/{id}")
    public ApiResponse<?> updateMethod(@PathVariable Integer id, @RequestBody PaymentMethodUpsertRequest request) {
        PaymentMethod method = findMethod(id);
        method.setMethodName(request.methodName());
        method.setDescription(request.description());
        method.setIsActive(request.active() == null ? method.getIsActive() : request.active());
        method.setUpdatedDate(LocalDateTime.now());
        return ApiResponse.success("Cập nhật phương thức thanh toán thành công", apiMapper.toPaymentMethodDto(paymentMethodRepository.save(method)));
    }

    @PostMapping("/methods/{id}/toggle-active")
    public ApiResponse<?> toggleMethod(@PathVariable Integer id) {
        PaymentMethod method = findMethod(id);
        method.setIsActive(!Boolean.TRUE.equals(method.getIsActive()));
        method.setUpdatedDate(LocalDateTime.now());
        return ApiResponse.success("Đã cập nhật trạng thái phương thức thanh toán", apiMapper.toPaymentMethodDto(paymentMethodRepository.save(method)));
    }

    @GetMapping("/transactions")
    public ApiResponse<Map<String, ?>> transactions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer methodId,
            @RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 50, Sort.by("transactionDate").descending());
        org.springframework.data.domain.Page<PaymentTransaction> transactions;
        if (status != null && !status.isBlank()) {
            transactions = paymentTransactionRepository.findByStatus(status, pageable);
        } else if (methodId != null) {
            transactions = paymentTransactionRepository.findByPaymentMethodPaymentMethodId(methodId, pageable);
        } else {
            transactions = paymentTransactionRepository.findAll(pageable);
        }
        return ApiResponse.success(Map.of(
                "transactions", PaginatedResponse.from(transactions, apiMapper::toPaymentTransactionDto),
                "paymentMethods", paymentMethodRepository.findAll().stream().map(apiMapper::toPaymentMethodDto).toList()));
    }

    @GetMapping("/transactions/{id}")
    public ApiResponse<?> transactionDetail(@PathVariable Integer id) {
        PaymentTransaction transaction = paymentTransactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));
        return ApiResponse.success(apiMapper.toPaymentTransactionDto(transaction));
    }

    private PaymentMethod findMethod(Integer id) {
        return paymentMethodRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phương thức thanh toán"));
    }
}
