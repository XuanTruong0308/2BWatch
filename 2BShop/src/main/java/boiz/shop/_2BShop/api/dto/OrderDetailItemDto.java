package boiz.shop._2BShop.api.dto;

import java.math.BigDecimal;

public record OrderDetailItemDto(
        Integer orderDetailId,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal discountAmount,
        BigDecimal subtotal,
        ProductCardDto watch) {
}
