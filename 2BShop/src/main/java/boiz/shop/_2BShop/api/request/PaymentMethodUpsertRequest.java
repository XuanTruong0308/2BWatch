package boiz.shop._2BShop.api.request;

public record PaymentMethodUpsertRequest(
        String methodName,
        String description,
        Boolean active) {
}
