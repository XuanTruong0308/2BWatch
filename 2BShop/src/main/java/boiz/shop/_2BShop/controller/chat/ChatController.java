package boiz.shop._2BShop.controller.chat;

import boiz.shop._2BShop.dto.chat.ChatMessage;
import boiz.shop._2BShop.service.SupportChatSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.time.LocalDateTime;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private SupportChatSessionService supportChatSessionService;

    @MessageMapping("/chat")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        if (!supportChatSessionService.isChatSessionActive(chatMessage.getSenderId())) {
            return;
        }

        chatMessage.setTimestamp(LocalDateTime.now());
        supportChatSessionService.appendMessage(chatMessage);
        
        // Gửi cho Admin (Admin sẽ subscribe kênh này)
        messagingTemplate.convertAndSend("/topic/admin", chatMessage);
        
        // Gửi lại cho đúng session của khách hàng (Khách subscribe kênh này)
        messagingTemplate.convertAndSend("/topic/chat." + chatMessage.getSenderId(), chatMessage);
    }
}
