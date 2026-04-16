package boiz.shop._2BShop.api.dto.chat;

import java.util.List;

import boiz.shop._2BShop.dto.chat.ChatMessage;

public record SupportChatSessionDto(
        String sessionId,
        String customerName,
        List<ChatMessage> messages,
        ChatMessage lastMessage) {
}
