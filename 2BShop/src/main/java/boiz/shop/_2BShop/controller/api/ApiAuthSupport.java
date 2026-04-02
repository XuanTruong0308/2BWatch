package boiz.shop._2BShop.controller.api;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.security.Principal;

public final class ApiAuthSupport {

    private ApiAuthSupport() {
    }

    public static String extractEmail(Principal principal) {
        if (principal == null) {
            return null;
        }

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        }

        if (principal instanceof OAuth2User) {
            Object email = ((OAuth2User) principal).getAttribute("email");
            if (email != null) {
                return email.toString();
            }
        }

        return principal.getName();
    }

    public static String currentAuthenticatedEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        String name = authentication.getName();
        if (name == null || "anonymousUser".equals(name)) {
            return null;
        }
        return name;
    }
}
