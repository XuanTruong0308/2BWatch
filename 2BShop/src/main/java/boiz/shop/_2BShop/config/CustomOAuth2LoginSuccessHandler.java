package boiz.shop._2BShop.config;

import boiz.shop._2BShop.entity.Cart;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.respository.CartRepository;
import boiz.shop._2BShop.service.CustomOAuth2User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component("customOAuth2LoginSuccessHandler")
public class CustomOAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CsrfTokenRepository csrfTokenRepository;

    @Value("${app.frontend.origin:http://localhost:5173}")
    private String frontendOrigin;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        refreshCsrfToken(request, response);
        
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        User user = oAuth2User.getUser();
        
        Cart cart = cartRepository.findByUser(user).orElse(null);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart.setCreatedDate(LocalDateTime.now());
            cart.setUpdatedDate(LocalDateTime.now());
            cartRepository.save(cart);
        }
        
        boolean isAdmin = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(role -> role.equals("ROLE_ADMIN"));
        
        if (isAdmin) {
            response.sendRedirect(frontendOrigin + "/admin/dashboard");
        } else {
            response.sendRedirect(frontendOrigin + "/");
        }
    }

    private void refreshCsrfToken(HttpServletRequest request, HttpServletResponse response) {
        CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
        csrfTokenRepository.saveToken(csrfToken, request, response);
        request.setAttribute(CsrfToken.class.getName(), csrfToken);
        request.setAttribute(csrfToken.getParameterName(), csrfToken);
    }
}
