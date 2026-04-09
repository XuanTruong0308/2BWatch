package boiz.shop._2BShop.api.dto;

import java.math.BigDecimal;

public record CartItemDto(
        Integer cartItemId,
        Integer quantity,
        Boolean selected,
        BigDecimal itemTotal,
        ProductCardDto watch) {
}
