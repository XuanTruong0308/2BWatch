package boiz.shop._2BShop.config;

import java.io.IOException;
import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class CustomLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final CsrfTokenRepository csrfTokenRepository;
    private final String frontendOrigin;

    public CustomLoginSuccessHandler(
            CsrfTokenRepository csrfTokenRepository,
            @Value("${app.frontend.origin:http://localhost:5173}") String frontendOrigin) {
        this.csrfTokenRepository = csrfTokenRepository;
        this.frontendOrigin = frontendOrigin;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {
        refreshCsrfToken(request, response);

        String continueUrl = request.getParameter("continue");
        if (continueUrl != null && !continueUrl.isBlank() && continueUrl.startsWith("/")) {
            getRedirectStrategy().sendRedirect(request, response, toFrontendUrl(continueUrl));
            return;
        }

        HttpSession session = request.getSession(false);
        SavedRequest savedRequest = null;
        if (session != null) {
            savedRequest = (SavedRequest) session.getAttribute("SPRING_SECURITY_SAVED_REQUEST");
        }

        if (savedRequest != null) {
            String savedRequestUrl = savedRequest.getRedirectUrl();
            URI savedRequestUri = URI.create(savedRequestUrl);
            String targetPath = savedRequestUri.getRawPath();
            String query = savedRequestUri.getRawQuery();
            if (query != null && !query.isBlank()) {
                targetPath += "?" + query;
            }
            getRedirectStrategy().sendRedirect(request, response, toFrontendUrl(targetPath));
            return;
        }

        String targetUrl;
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            targetUrl = "/admin/dashboard";
        } else {
            targetUrl = "/";
        }

        getRedirectStrategy().sendRedirect(request, response, toFrontendUrl(targetUrl));
    }

    private void refreshCsrfToken(HttpServletRequest request, HttpServletResponse response) {
        CsrfToken csrfToken = csrfTokenRepository.generateToken(request);
        csrfTokenRepository.saveToken(csrfToken, request, response);
        request.setAttribute(CsrfToken.class.getName(), csrfToken);
        request.setAttribute(csrfToken.getParameterName(), csrfToken);
    }

    private String toFrontendUrl(String path) {
        return frontendOrigin + path;
    }
}
