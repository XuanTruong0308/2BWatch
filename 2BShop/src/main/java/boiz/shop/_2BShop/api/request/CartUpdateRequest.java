package boiz.shop._2BShop.api.request;

public record CartUpdateRequest(
        Integer cartItemId,
        Integer quantity) {
}
