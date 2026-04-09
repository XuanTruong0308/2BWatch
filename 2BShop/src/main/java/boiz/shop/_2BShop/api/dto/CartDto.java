package boiz.shop._2BShop.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record CartDto(
        List<CartItemDto> items,
        BigDecimal subtotal,
        BigDecimal shippingFee,
        BigDecimal total,
        int totalItemCount,
        int selectedItemCount) {
}
