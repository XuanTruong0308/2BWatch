package boiz.shop._2BShop.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        
        // Kiểm tra xem có saved request không (user đang cố truy cập trang nào đó trước khi login)
        HttpSession session = request.getSession(false);
        SavedRequest savedRequest = null;
        if (session != null) {
            savedRequest = (SavedRequest) session.getAttribute("SPRING_SECURITY_SAVED_REQUEST");
        }
        
        // Nếu có saved request, ưu tiên redirect về trang đó
        if (savedRequest != null) {
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }
        
        // Nếu không có saved request, redirect theo role
        String targetUrl;
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            targetUrl = "/admin/dashboard";
        } else {
            targetUrl = "/";
        }
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
