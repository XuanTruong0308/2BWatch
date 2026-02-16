# Hướng dẫn Tích hợp Đăng nhập Google và Facebook với OAuth2

## Tổng quan
Hướng dẫn này sẽ giúp bạn tích hợp đăng nhập qua Google và Facebook vào ứng dụng Spring Boot của bạn sử dụng Spring Security OAuth2.

## Bước 1: Thêm Dependencies vào pom.xml

```xml
<!-- OAuth2 Client -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>

<!-- Optional: Để lưu OAuth2 token vào database -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-oauth2-jose</artifactId>
</dependency>
```

## Bước 2: Đăng ký Ứng dụng với Google và Facebook

### 2.1. Google OAuth2 Setup

1. **Truy cập Google Cloud Console:**
   - Vào https://console.cloud.google.com/
   - Tạo project mới hoặc chọn project có sẵn

2. **Tạo OAuth2 Credentials:**
   - Vào **APIs & Services** > **Credentials**
   - Click **Create Credentials** > **OAuth client ID**
   - Chọn **Application type**: Web application
   - **Authorized JavaScript origins**: `http://localhost:8080`
   - **Authorized redirect URIs**: `http://localhost:8080/login/oauth2/code/google`
   - Click **Create**

3. **Lưu thông tin:**
   - **Client ID**: Ví dụ `123456789-abc.apps.googleusercontent.com`
   - **Client Secret**: Ví dụ `GOCSPX-xxxxxxxxxxxxx`

### 2.2. Facebook OAuth2 Setup

1. **Truy cập Facebook Developers:**
   - Vào https://developers.facebook.com/
   - Click **My Apps** > **Create App**

2. **Chọn App Type:**
   - Chọn **Consumer**
   - Điền tên app và email liên hệ

3. **Cấu hình Facebook Login:**
   - Vào **Dashboard** > **Add Product** > **Facebook Login** > **Set Up**
   - Chọn **Web**
   - **Site URL**: `http://localhost:8080`
   - Vào **Facebook Login** > **Settings**
   - **Valid OAuth Redirect URIs**: `http://localhost:8080/login/oauth2/code/facebook`
   - Click **Save Changes**

4. **Lưu thông tin:**
   - Vào **Settings** > **Basic**
   - **App ID**: Ví dụ `123456789012345`
   - **App Secret**: Click **Show** để xem (Ví dụ `abcdef1234567890abcdef1234567890`)

## Bước 3: Cấu hình application.properties

Thêm cấu hình OAuth2 vào file `src/main/resources/application.properties`:

```properties
# OAuth2 Configuration
# Google OAuth2
spring.security.oauth2.client.registration.google.client-id=YOUR_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.registration.google.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}

# Facebook OAuth2
spring.security.oauth2.client.registration.facebook.client-id=YOUR_FACEBOOK_APP_ID
spring.security.oauth2.client.registration.facebook.client-secret=YOUR_FACEBOOK_APP_SECRET
spring.security.oauth2.client.registration.facebook.scope=public_profile,email
spring.security.oauth2.client.registration.facebook.redirect-uri={baseUrl}/login/oauth2/code/{registrationId}

# Provider Configuration (Optional - Spring Boot tự động cấu hình)
spring.security.oauth2.client.provider.google.authorization-uri=https://accounts.google.com/o/oauth2/v2/auth
spring.security.oauth2.client.provider.google.token-uri=https://oauth2.googleapis.com/token
spring.security.oauth2.client.provider.google.user-info-uri=https://www.googleapis.com/oauth2/v3/userinfo
spring.security.oauth2.client.provider.google.user-name-attribute=sub

spring.security.oauth2.client.provider.facebook.authorization-uri=https://www.facebook.com/v12.0/dialog/oauth
spring.security.oauth2.client.provider.facebook.token-uri=https://graph.facebook.com/v12.0/oauth/access_token
spring.security.oauth2.client.provider.facebook.user-info-uri=https://graph.facebook.com/me?fields=id,name,email,picture
spring.security.oauth2.client.provider.facebook.user-name-attribute=id
```

**Lưu ý:** Thay `YOUR_GOOGLE_CLIENT_ID`, `YOUR_GOOGLE_CLIENT_SECRET`, `YOUR_FACEBOOK_APP_ID`, `YOUR_FACEBOOK_APP_SECRET` bằng giá trị thực tế từ bước 2.

## Bước 4: Cập nhật SecurityConfig.java

Cập nhật file `SecurityConfig.java` để hỗ trợ OAuth2:

```java
package boiz.shop._2BShop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/watches/**", "/login", "/register", "/verify", 
                                "/forgot-password", "/reset-password", 
                                "/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**", "/cart/**", "/checkout/**", "/orders/**").hasRole("USER")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error=true")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService) // Inject custom service
                )
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .csrf(csrf -> csrf.disable()); // Disable for development, enable in production
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## Bước 5: Tạo CustomOAuth2UserService

Tạo service để xử lý thông tin user từ OAuth2:

**File:** `src/main/java/boiz/shop/_2BShop/service/CustomOAuth2UserService.java`

```java
package boiz.shop._2BShop.service;

import boiz.shop._2BShop.entity.Role;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.entity.UserRole;
import boiz.shop._2BShop.respository.RoleRepository;
import boiz.shop._2BShop.respository.UserRepository;
import boiz.shop._2BShop.respository.UserRoleRepository;
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
    private UserRoleRepository userRoleRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        // Get provider (google or facebook)
        String provider = userRequest.getClientRegistration().getRegistrationId();
        
        // Process user info based on provider
        String providerId;
        String email;
        String name;
        
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        if ("google".equals(provider)) {
            providerId = (String) attributes.get("sub");
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
        } else if ("facebook".equals(provider)) {
            providerId = attributes.get("id").toString();
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
        } else {
            throw new OAuth2AuthenticationException("Unsupported provider: " + provider);
        }
        
        // Check if user already exists
        Optional<User> existingUser = userRepository.findByOauth2ProviderAndOauth2ProviderId(provider, providerId);
        
        User user;
        if (existingUser.isPresent()) {
            // User exists, update last login
            user = existingUser.get();
        } else {
            // Create new user
            user = new User();
            user.setUsername(email.split("@")[0] + "_" + provider); // username = email prefix + provider
            user.setEmail(email);
            user.setFullName(name);
            user.setOauth2Provider(provider);
            user.setOauth2ProviderId(providerId);
            user.setIsVerified(true); // OAuth2 users are pre-verified
            user.setIsActive(true);
            user.setCreatedDate(LocalDateTime.now());
            
            userRepository.save(user);
            
            // Assign USER role
            Role userRole = roleRepository.findByRoleName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role USER not found"));
            
            UserRole userRoleMapping = new UserRole();
            userRoleMapping.setUser(user);
            userRoleMapping.setRole(userRole);
            userRoleRepository.save(userRoleMapping);
        }
        
        return oAuth2User;
    }
}
```

## Bước 6: Cập nhật Entity User

Thêm các field OAuth2 vào entity `User.java`:

```java
// Thêm vào class User
@Column(name = "oauth2_provider")
private String oauth2Provider; // "google" hoặc "facebook"

@Column(name = "oauth2_provider_id")
private String oauth2ProviderId; // ID từ provider

// Getters và Setters
public String getOauth2Provider() {
    return oauth2Provider;
}

public void setOauth2Provider(String oauth2Provider) {
    this.oauth2Provider = oauth2Provider;
}

public String getOauth2ProviderId() {
    return oauth2ProviderId;
}

public void setOauth2ProviderId(String oauth2ProviderId) {
    this.oauth2ProviderId = oauth2ProviderId;
}
```

## Bước 7: Cập nhật Database Schema

Thêm vào file `src/main/resources/db/schema.sql`:

```sql
-- Thêm columns cho OAuth2 vào bảng Users
ALTER TABLE Users ADD oauth2_provider VARCHAR(20) NULL;
ALTER TABLE Users ADD oauth2_provider_id VARCHAR(255) NULL;

-- Tạo index cho tìm kiếm nhanh
CREATE INDEX idx_oauth2_provider ON Users(oauth2_provider, oauth2_provider_id);
```

## Bước 8: Cập nhật login.html

File `login.html` đã được cập nhật với các button OAuth2. Đảm bảo form action đúng:

```html
<!-- Google Login Button -->
<form action="/oauth2/authorization/google" method="get" style="margin-bottom: 10px;">
    <button type="submit" class="btn btn-social btn-google">
        <i class="fab fa-google"></i>
        Đăng nhập với Google
    </button>
</form>

<!-- Facebook Login Button -->
<form action="/oauth2/authorization/facebook" method="get">
    <button type="submit" class="btn btn-social btn-facebook">
        <i class="fab fa-facebook-f"></i>
        Đăng nhập với Facebook
    </button>
</form>
```

## Bước 9: CSS cho OAuth2 Buttons

Thêm vào `style.css`:

```css
/* OAuth2 Social Login Buttons */
.btn-social {
    width: 100%;
    padding: 12px;
    border: none;
    border-radius: 5px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
}

.btn-google {
    background: #db4437;
    color: white;
}

.btn-google:hover {
    background: #c33d2e;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(219, 68, 55, 0.4);
}

.btn-facebook {
    background: #4267B2;
    color: white;
}

.btn-facebook:hover {
    background: #365899;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(66, 103, 178, 0.4);
}

.btn-social i {
    font-size: 18px;
}
```

## Bước 10: Test OAuth2 Login

### Test với Google:
1. Mở `http://localhost:8080/login`
2. Click "Đăng nhập với Google"
3. Chọn Google account
4. Cho phép ứng dụng truy cập
5. Redirect về homepage, đã login thành công

### Test với Facebook:
1. Mở `http://localhost:8080/login`
2. Click "Đăng nhập với Facebook"
3. Đăng nhập Facebook account
4. Cho phép ứng dụng
5. Redirect về homepage, đã login thành công

### Kiểm tra Database:
```sql
SELECT * FROM Users WHERE oauth2_provider IS NOT NULL;
```

Bạn sẽ thấy:
- `oauth2_provider`: "google" hoặc "facebook"
- `oauth2_provider_id`: ID từ provider
- `is_verified`: true (tự động verified)

## Troubleshooting

### Lỗi: redirect_uri_mismatch
**Nguyên nhân:** URL redirect không khớp với config trên Google/Facebook

**Giải pháp:**
- Kiểm tra URL trong Google Console / Facebook App Settings
- Đảm bảo: `http://localhost:8080/login/oauth2/code/google`
- Không có trailing slash
- HTTP (local) hoặc HTTPS (production)

### Lỗi: Email null từ Facebook
**Nguyên nhân:** Facebook không trả về email

**Giải pháp:**
```java
// Handle null email
if (email == null || email.isEmpty()) {
    email = providerId + "@facebook.user";
}
```

### Lỗi: Role USER not found
**Nguyên nhân:** Database chưa có role USER

**Giải pháp:**
```sql
-- Chạy script này để tạo role
INSERT INTO Roles (role_name, description, created_date)
VALUES ('ROLE_USER', 'Normal User', GETDATE());
```

## Best Practices

1. **Production Environment:**
   - Dùng HTTPS cho redirect URI
   - Store Client Secret trong environment variables
   - Enable CSRF protection

2. **Security:**
   - Validate email từ OAuth2 provider
   - Check user status (banned/active)
   - Rate limiting cho OAuth2 endpoints

3. **User Experience:**
   - Show loading spinner khi redirect to provider
   - Handle OAuth2 errors gracefully
   - Provide option to link existing account

## Kết luận

Bạn đã tích hợp thành công OAuth2 login cho Google và Facebook! Người dùng giờ có thể:
- Đăng nhập nhanh chóng với Google/Facebook account
- Không cần nhớ thêm password
- Tự động verified email

**Next Steps:**
- Test trên production với HTTPS
- Thêm OAuth2 providers khác (GitHub, Twitter, etc.)
- Implement account linking (link OAuth2 với existing account)
