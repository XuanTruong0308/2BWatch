package boiz.shop._2BShop.api.request;

public record ConfirmRegisterRequest(
        String email,
        String token) {
}
