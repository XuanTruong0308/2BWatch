package boiz.shop._2BShop.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Custom User Principal that extends Spring Security User
 * to include additional user information like fullName, userId
 */
public class CustomUserPrincipal extends User {
    
    private final Integer userId;
    private final String fullName;
    private final String email;

    public CustomUserPrincipal(
            Integer userId,
            String email, 
            String password,
            String fullName,
            Boolean enabled,
            boolean accountNonExpired,
            boolean credentialsNonExpired,
            boolean accountNonLocked,
            Collection<? extends GrantedAuthority> authorities) {
        
        super(email, password, 
              enabled != null ? enabled : false, // Convert Boolean to boolean
              accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }
}
