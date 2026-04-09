package boiz.shop._2BShop.api.dto;

import java.time.LocalDateTime;

public record BankAccountDto(
        Integer bankAccountId,
        String bankName,
        String bankCode,
        String accountNumber,
        String accountHolder,
        String qrImageUrl,
        Boolean active,
        Integer displayOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
