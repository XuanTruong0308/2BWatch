package boiz.shop._2BShop.api.controller.user;

import java.math.BigDecimal;
import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import boiz.shop._2BShop.api.dto.CartDto;
import boiz.shop._2BShop.api.dto.CheckoutContextDto;
import boiz.shop._2BShop.api.dto.OrderDto;
import boiz.shop._2BShop.api.mapper.ApiMapper;
import boiz.shop._2BShop.api.request.CheckoutPlaceOrderRequest;
import boiz.shop._2BShop.api.response.ApiResponse;
import boiz.shop._2BShop.api.util.CurrentUserService;
import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.respository.PaymentMethodRepository;
import boiz.shop._2BShop.service.BankAccountService;
import boiz.shop._2BShop.service.CartService;
import boiz.shop._2BShop.service.CheckoutService;
import boiz.shop._2BShop.service.PhoneVerificationService;

@RestController
@RequestMapping("/api/v1/checkout")
public class CheckoutApiController {

    private final CheckoutService checkoutService;
    private final CartService cartService;
    private final BankAccountService bankAccountService;
    private final PaymentMethodRepository paymentMethodRepository;
    private final CurrentUserService currentUserService;
    private final ApiMapper apiMapper;
    private final PhoneVerificationService phoneVerificationService;

    public CheckoutApiController(
            CheckoutService checkoutService,
            CartService cartService,
            BankAccountService bankAccountService,
            PaymentMethodRepository paymentMethodRepository,
            CurrentUserService currentUserService,
            ApiMapper apiMapper,
            PhoneVerificationService phoneVerificationService) {
        this.checkoutService = checkoutService;
        this.cartService = cartService;
        this.bankAccountService = bankAccountService;
        this.paymentMethodRepository = paymentMethodRepository;
        this.currentUserService = currentUserService;
        this.apiMapper = apiMapper;
        this.phoneVerificationService = phoneVerificationService;
    }

    @GetMapping
    public ApiResponse<CheckoutContextDto> checkout(@RequestParam(required = false) String couponCode) {
        User user = currentUserService.getCurrentUserOrThrow();
        validateCheckoutEligibility(user);
        CartDto cart = buildCartDto();
        if (cart.items().isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
        }
        return ApiResponse.success(new CheckoutContextDto(
                apiMapper.toAuthUserDto(user, true),
                cart,
                apiMapper.toCheckoutSummaryDto(checkoutService.calculateTotals(couponCode)),
                paymentMethodRepository.findByIsActiveTrueOrderByMethodName().stream()
                        .map(apiMapper::toPaymentMethodDto)
                        .toList(),
                bankAccountService.getActiveBankAccounts().stream()
                        .map(apiMapper::toBankAccountDto)
                        .toList()));
    }

    @PostMapping("/place-order")
    public ApiResponse<OrderDto> placeOrder(@RequestBody CheckoutPlaceOrderRequest request, Principal principal) {
        String email = currentUserService.extractEmail(principal);
        User user = email == null ? null : currentUserService.getCurrentUserOrThrow();
        validateCheckoutEligibility(user);
        Order order = checkoutService.placeOrder(
                user,
                request.receiverName(),
                request.phone(),
                request.address(),
                request.notes(),
                request.paymentMethod(),
                request.couponCode(),
                request.bankAccountId());
        return ApiResponse.success("Đặt hàng thành công", apiMapper.toOrderDto(order));
    }

    @GetMapping("/confirmation/{orderId}")
    public ApiResponse<OrderDto> confirmation(@PathVariable Integer orderId) {
        User user = currentUserService.getCurrentUserOrThrow();
        Order order = checkoutService.getOrderById(orderId);
        if (order.getUser() == null || !order.getUser().getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này");
        }
        return ApiResponse.success(apiMapper.toOrderDto(order));
    }

    private CartDto buildCartDto() {
        BigDecimal subtotal = cartService.calculateSubtotal();
        BigDecimal shippingFee = subtotal.compareTo(new BigDecimal("500000")) >= 0
                ? BigDecimal.ZERO
                : new BigDecimal("30000");
        BigDecimal total = subtotal.add(shippingFee);
        return apiMapper.toCartDto(cartService.getSelectedCartItems(), subtotal, shippingFee, total);
    }

    private void validateCheckoutEligibility(User user) {
        if (user != null
                && "GOOGLE".equalsIgnoreCase(user.getProvider())
                && phoneVerificationService.needsPhoneVerification(user)) {
            throw new RuntimeException("Vui lòng cập nhật số điện thoại để tiếp tục đặt hàng");
        }
    }
}
