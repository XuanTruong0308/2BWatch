package boiz.shop._2BShop.api.dto;

import java.math.BigDecimal;

public record ProductCardDto(
        Integer watchId,
        String watchName,
        String description,
        String brandName,
        String categoryName,
        String imageUrl,
        BigDecimal price,
        BigDecimal priceAfterDiscount,
        Integer discountPercent,
        Integer stockQuantity,
        Integer soldCount,
        Boolean active) {
}
