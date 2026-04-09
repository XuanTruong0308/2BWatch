package boiz.shop._2BShop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import boiz.shop._2BShop.service.CustomOAuth2UserService;
import boiz.shop._2BShop.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

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
    public CookieCsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookiePath("/");
        return repository;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            CookieCsrfTokenRepository csrfTokenRepository,
            CustomLoginSuccessHandler loginSuccessHandler,
            @Qualifier("customOAuth2LoginSuccessHandler") AuthenticationSuccessHandler oAuth2LoginSuccessHandler)
            throws Exception {
        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html")
                        .permitAll()

                        .requestMatchers("/api/v1/auth/**", "/api/v1/public/**")
                        .permitAll()

                        .requestMatchers("/api/v1/admin/**")
                        .hasRole("ADMIN")

                        .requestMatchers("/api/v1/profile/**")
                        .hasRole("USER")

                        .requestMatchers("/api/v1/orders/**", "/api/v1/checkout/**")
                        .authenticated()

                        .requestMatchers("/api/v1/cart/count")
                        .permitAll()

                        .requestMatchers("/api/v1/cart/**")
                        .authenticated()

                        .requestMatchers("/api/v1/**")
                        .authenticated()

                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/forgot-password",
                                "/reset-password",
                                "/confirm-register",
                                "/about",
                                "/contact",
                                "/policy",
                                "/terms",
                                "/faq",
                                "/payment-result",
                                "/watches/**",
                                "/products/**",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/perform-login",
                                "/static/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/img/**",
                                "/video/**",
                                "/uploads/**",
                                "/assets/**",
                                "/favicon.ico",
                                "/favicon.png",
                                "/error")
                        .permitAll()

                        .requestMatchers("/payment/vnpay-return")
                        .permitAll()

                        .requestMatchers("/cart/**", "/checkout/**", "/my-orders/**", "/user/**", "/invoice/**")
                        .authenticated()

                        .requestMatchers("/profile/**", "/account/**")
                        .hasRole("USER")

                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        .anyRequest()
                        .permitAll())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform-login")
                        .successHandler(loginSuccessHandler)
                        .failureUrl("/login?error=true")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureUrl("/login?error=oauth2"))
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/**"))
                        .defaultAccessDeniedHandlerFor(
                                (request, response, accessDeniedException) -> response.sendError(HttpStatus.FORBIDDEN.value()),
                                new AntPathRequestMatcher("/api/**")));

        http.addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class);

        return http.build();
    }
}
