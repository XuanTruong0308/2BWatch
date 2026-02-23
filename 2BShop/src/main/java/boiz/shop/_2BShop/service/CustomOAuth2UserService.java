package boiz.shop._2BShop.service;

import boiz.shop._2BShop.entity.Role;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.entity.UserRole;
import boiz.shop._2BShop.respository.RoleRepository;
import boiz.shop._2BShop.respository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private boiz.shop._2BShop.respository.UserRoleRepository userRoleRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        String email = extractEmail(provider, attributes);
        String name = extractName(provider, attributes);
        String providerId = extractProviderId(provider, attributes);
        String avatarUrl = extractAvatarUrl(provider, attributes);
        
        User user = processOAuthUser(email, name, provider, providerId, avatarUrl);
        
        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }

    private String extractEmail(String provider, Map<String, Object> attributes) {
        if ("google".equalsIgnoreCase(provider)) {
            return (String) attributes.get("email");
        } else if ("facebook".equalsIgnoreCase(provider)) {
            return (String) attributes.get("email");
        }
        return null;
    }

    private String extractName(String provider, Map<String, Object> attributes) {
        if ("google".equalsIgnoreCase(provider)) {
            return (String) attributes.get("name");
        } else if ("facebook".equalsIgnoreCase(provider)) {
            return (String) attributes.get("name");
        }
        return null;
    }

    private String extractProviderId(String provider, Map<String, Object> attributes) {
        if ("google".equalsIgnoreCase(provider)) {
            return (String) attributes.get("sub");
        } else if ("facebook".equalsIgnoreCase(provider)) {
            return (String) attributes.get("id");
        }
        return null;
    }

    private String extractAvatarUrl(String provider, Map<String, Object> attributes) {
        if ("google".equalsIgnoreCase(provider)) {
            return (String) attributes.get("picture");
        } else if ("facebook".equalsIgnoreCase(provider)) {
            @SuppressWarnings("unchecked")
            Map<String, Object> picture = (Map<String, Object>) attributes.get("picture");
            if (picture != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) picture.get("data");
                if (data != null) {
                    return (String) data.get("url");
                }
            }
        }
        return null;
    }

    @Transactional
    private User processOAuthUser(String email, String name, String provider, String providerId, String avatarUrl) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            
            if (!provider.equalsIgnoreCase(user.getProvider())) {
                user.setProvider(provider.toUpperCase());
                user.setProviderId(providerId);
            }
            
            if (avatarUrl != null && (user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty())) {
                user.setAvatarUrl(avatarUrl);
            }
            
            if (!Boolean.TRUE.equals(user.getEmailVerified())) {
                user.setEmailVerified(true);
            }
            
            if (!Boolean.TRUE.equals(user.getIsEnabled())) {
                user.setIsEnabled(true);
            }
            
            // OAuth2 users need phone verification if not already verified
            if ((user.getPhone() == null || user.getPhone().isEmpty()) && !Boolean.TRUE.equals(user.getPhoneVerified())) {
                user.setPhoneVerified(false);
            }
            
            user.setUpdatedDate(LocalDateTime.now());
            userRepository.save(user);
            
            // Reload user with UserRoles to ensure authorities are loaded
            return userRepository.findById(user.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found after update"));
        } else {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(name);
            newUser.setUsername(generateUsername(email));
            newUser.setProvider(provider.toUpperCase());
            newUser.setProviderId(providerId);
            newUser.setAvatarUrl(avatarUrl);
            newUser.setEmailVerified(true);
            newUser.setPhoneVerified(false);  // OAuth2 users must update phone
            newUser.setIsEnabled(true);
            newUser.setIsBanned(false);
            newUser.setCreatedDate(LocalDateTime.now());
            newUser.setUpdatedDate(LocalDateTime.now());
            
            User savedUser = userRepository.save(newUser);
            
            Role userRole = roleRepository.findByRoleName("USER")
                .orElseThrow(() -> new RuntimeException("USER role not found"));
            
            UserRole ur = new UserRole();
            ur.setUser(savedUser);
            ur.setRole(userRole);
            userRoleRepository.save(ur);  // Save UserRole to database
            
            // Reload user with UserRoles
            return userRepository.findById(savedUser.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found after save"));
        }
    }

    private String generateUsername(String email) {
        String baseUsername = email.split("@")[0];
        String username = baseUsername;
        int counter = 1;
        
        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + counter;
            counter++;
        }
        
        return username;
    }
}
