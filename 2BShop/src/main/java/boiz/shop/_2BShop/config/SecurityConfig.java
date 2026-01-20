package boiz.shop._2BShop.config;

import boiz.shop._2BShop.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private CustomLoginSuccessHandler loginSuccessHandler;

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth, PasswordEncoder passwordEncoder) throws Exception {
        auth.userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Public pages - KHÔNG cần đăng nhập
                .requestMatchers("/", "/login", "/register", "/account/login", "/account/register",
                    "/watches/**", "/products/**", "/verify", "/confirm-register", "/resend-verification",
                    "/static/**", "/css/**", "/js/**", "/images/**", "/uploads/**", "/error").permitAll()
                
                // User pages - CẦN đăng nhập (USER role)
                .requestMatchers("/cart/**", "/checkout/**", "/payment/**", "/orders/**", 
                    "/profile/**", "/account").hasRole("USER")
                
                // Admin pages - CẦN đăng nhập (ADMIN role)
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // Tất cả còn lại - Public (có thể 404 nếu không tồn tại)
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/perform-login")
                .successHandler(loginSuccessHandler)
                .failureUrl("/login?error=true")
                .usernameParameter("email")
                .passwordParameter("password")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );
        
        return http.build();
    }
}
