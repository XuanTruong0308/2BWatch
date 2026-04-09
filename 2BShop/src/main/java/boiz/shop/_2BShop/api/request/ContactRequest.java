package boiz.shop._2BShop.api.request;

public record ContactRequest(
        String name,
        String email,
        String subject,
        String message) {
}
