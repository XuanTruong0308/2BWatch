package boiz.shop._2BShop.api.request;

public record CartSelectionRequest(
        Integer cartItemId,
        Boolean isSelected) {
}
