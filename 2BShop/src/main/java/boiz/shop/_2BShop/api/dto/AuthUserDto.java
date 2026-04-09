package boiz.shop._2BShop.api.dto;

import java.util.List;

public record AuthUserDto(
        Integer userId,
        String username,
        String email,
        String fullName,
        String phone,
        String address,
        String avatarUrl,
        String provider,
        Boolean phoneVerified,
        Boolean emailVerified,
        Boolean enabled,
        Boolean authenticated,
        Boolean admin,
        List<String> roles) {
}
