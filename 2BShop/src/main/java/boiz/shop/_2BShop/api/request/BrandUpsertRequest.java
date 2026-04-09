package boiz.shop._2BShop.api.request;

public record BrandUpsertRequest(
        String brandName,
        String description,
        String logoUrl,
        Boolean active) {
}
