package boiz.shop._2BShop.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record ProductDetailDto(
        Integer watchId,
        String watchName,
        String description,
        String brandName,
        Integer brandId,
        String categoryName,
        Integer categoryId,
        BigDecimal price,
        BigDecimal priceAfterDiscount,
        Integer discountPercent,
        Integer stockQuantity,
        Integer soldCount,
        Boolean active,
        List<ImageDto> images,
        List<ProductCardDto> relatedProducts) {
}
