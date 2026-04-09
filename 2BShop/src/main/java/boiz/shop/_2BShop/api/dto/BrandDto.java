package boiz.shop._2BShop.api.dto;

public record BrandDto(
        Integer brandId,
        String brandName,
        String description,
        String logoUrl,
        Boolean active,
        long watchCount) {
}
