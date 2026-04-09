package boiz.shop._2BShop.api.dto;

import java.util.List;

public record CheckoutContextDto(
        AuthUserDto user,
        CartDto cart,
        CheckoutSummaryDto summary,
        List<PaymentMethodDto> paymentMethods,
        List<BankAccountDto> bankAccounts) {
}
