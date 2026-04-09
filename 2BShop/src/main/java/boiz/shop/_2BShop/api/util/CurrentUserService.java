package boiz.shop._2BShop.api.util;

import java.security.Principal;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.respository.UserRepository;

@Component
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName());
    }

    public String extractEmail(Principal principal) {
        if (principal == null) {
            return null;
        }
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof OAuth2User oauth2User) {
            Object emailAttr = oauth2User.getAttribute("email");
            return emailAttr != null ? emailAttr.toString() : oauth2User.getName();
        }
        return principal.getName();
    }

    public User getCurrentUser() {
        if (!isAuthenticated()) {
            return null;
        }
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    public User getCurrentUserOrThrow() {
        User user = getCurrentUser();
        if (user == null) {
            throw new RuntimeException("Vui lòng đăng nhập để tiếp tục");
        }
        return user;
    }

    public List<String> getRoleNames(User user) {
        return user.getUserRoles().stream()
                .map(userRole -> userRole.getRole().getRoleName())
                .toList();
    }

    public boolean isAdmin(User user) {
        return getRoleNames(user).contains("ADMIN");
    }
}
