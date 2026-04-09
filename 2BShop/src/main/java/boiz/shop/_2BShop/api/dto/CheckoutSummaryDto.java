package boiz.shop._2BShop.api.dto;

import java.math.BigDecimal;

public record CheckoutSummaryDto(
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal shippingFee,
        BigDecimal totalAmount,
        BigDecimal depositAmount,
        boolean depositRequired,
        String couponCode) {
}
