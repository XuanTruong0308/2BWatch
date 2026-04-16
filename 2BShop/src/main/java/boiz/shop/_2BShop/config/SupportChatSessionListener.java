package boiz.shop._2BShop.config;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import boiz.shop._2BShop.api.dto.chat.SupportChatSessionEventDto;
import boiz.shop._2BShop.service.SupportChatSessionService;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionIdListener;
import jakarta.servlet.http.HttpSessionListener;

@Component
public class SupportChatSessionListener implements HttpSessionListener, HttpSessionIdListener {

    private final SupportChatSessionService supportChatSessionService;
    private final SimpMessagingTemplate messagingTemplate;

    public SupportChatSessionListener(
            SupportChatSessionService supportChatSessionService,
            SimpMessagingTemplate messagingTemplate) {
        this.supportChatSessionService = supportChatSessionService;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        publishSessionRemoval(supportChatSessionService.clearByHttpSessionId(se.getSession().getId(), "SESSION_EXPIRED"));
    }

    @Override
    public void sessionIdChanged(HttpSessionEvent se, String oldSessionId) {
        supportChatSessionService.rebindHttpSession(se.getSession(), oldSessionId);
    }

    private void publishSessionRemoval(SupportChatSessionEventDto event) {
        if (event == null) {
            return;
        }

        messagingTemplate.convertAndSend("/topic/admin.chat.sessions", event);
        messagingTemplate.convertAndSend("/topic/chat.session." + event.sessionId(), event);
    }
}
