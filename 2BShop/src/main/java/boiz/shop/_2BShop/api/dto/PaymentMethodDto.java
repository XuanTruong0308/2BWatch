package boiz.shop._2BShop.api.dto;

import java.time.LocalDateTime;

public record PaymentMethodDto(
        Integer paymentMethodId,
        String methodName,
        String description,
        Boolean active,
        LocalDateTime createdDate,
        LocalDateTime updatedDate) {
}
