package boiz.shop._2BShop.api.dto;

public record OptionDto(
        Integer id,
        String value,
        String label,
        Boolean active) {
}
