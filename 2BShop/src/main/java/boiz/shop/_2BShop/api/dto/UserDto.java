package boiz.shop._2BShop.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record UserDto(
        Integer userId,
        String username,
        String email,
        String fullName,
        String phone,
        String address,
        String avatarUrl,
        String provider,
        Boolean emailVerified,
        Boolean phoneVerified,
        Boolean enabled,
        Boolean banned,
        LocalDateTime createdDate,
        LocalDateTime updatedDate,
        List<String> roles,
        Long orderCount,
        BigDecimal totalSpent) {
}
