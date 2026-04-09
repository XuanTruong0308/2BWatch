package boiz.shop._2BShop.api.request;

public record BankAccountUpsertRequest(
        String bankName,
        String bankCode,
        String accountNumber,
        String accountHolder,
        Boolean active,
        Integer displayOrder) {
}
