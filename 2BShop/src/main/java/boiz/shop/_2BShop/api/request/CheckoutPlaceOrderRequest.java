package boiz.shop._2BShop.api.request;

public record CheckoutPlaceOrderRequest(
        String receiverName,
        String phone,
        String address,
        String paymentMethod,
        String notes,
        String couponCode,
        Integer bankAccountId) {
}
