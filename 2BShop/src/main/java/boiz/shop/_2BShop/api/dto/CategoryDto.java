package boiz.shop._2BShop.api.dto;

public record CategoryDto(
        Integer categoryId,
        String categoryName,
        String description,
        Boolean active) {
}
