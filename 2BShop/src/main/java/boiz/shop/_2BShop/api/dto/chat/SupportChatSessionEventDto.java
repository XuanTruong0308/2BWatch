package boiz.shop._2BShop.api.dto.chat;

public record SupportChatSessionEventDto(
        String type,
        String sessionId,
        String reason) {
}
