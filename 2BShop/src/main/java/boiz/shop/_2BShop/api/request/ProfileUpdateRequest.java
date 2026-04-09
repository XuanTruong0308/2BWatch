package boiz.shop._2BShop.api.request;

public record ProfileUpdateRequest(
        String fullName,
        String phone,
        String address) {
}
