package boiz.shop._2BShop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;

@Service
public class SessionService {
    @Autowired
    HttpSession session;

    //Luu data vao session
    public void set(String name, Object value) {
        session.setAttribute(name, value);
    }

    //Lay du lieu tu session (dung generic<T> de tu ep kieu)
    public <T> T get(String name) {
        return (T) session.getAttribute(name);
    }

    //Xoa du lieu trong session
    public void remove(String name) {
        session.removeAttribute(name);
    }

}
