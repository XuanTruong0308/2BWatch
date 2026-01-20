package boiz.shop._2BShop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.*;

//Dung cho remember me
@Service
public class CookieService {
    @Autowired
    HttpServletRequest req;

    @Autowired
    HttpServletResponse res;

    public Cookie get(String name) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if(cookie.getName().equalsIgnoreCase(name)) return cookie;
            }
        }
        return null;
    }

    public void add(String name, String value, int days) {
        Cookie cookie = new Cookie(name, value);
        cookie.setMaxAge(days * 24 * 60 * 60);
        cookie.setPath("/");
        res.addCookie(cookie);
    }

    public void remove(String name) {
        add(name, "", 0);
    };
    
}
