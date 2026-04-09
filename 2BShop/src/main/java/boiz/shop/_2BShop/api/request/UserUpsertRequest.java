package boiz.shop._2BShop.api.request;

import java.util.List;

public record UserUpsertRequest(
        String username,
        String email,
        String fullName,
        String phone,
        String address,
        String avatarUrl,
        Boolean enabled,
        String newPassword,
        List<String> roleNames) {
}
