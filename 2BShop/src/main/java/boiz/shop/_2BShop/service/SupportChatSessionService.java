package boiz.shop._2BShop.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

import boiz.shop._2BShop.api.dto.chat.SupportChatBootstrapDto;
import boiz.shop._2BShop.api.dto.chat.SupportChatSessionDto;
import boiz.shop._2BShop.api.dto.chat.SupportChatSessionEventDto;
import boiz.shop._2BShop.dto.chat.ChatMessage;
import jakarta.servlet.http.HttpSession;

@Service
public class SupportChatSessionService {

    private static final String CHAT_SESSION_KEY = "support_chat_session_id";
    private static final String DEFAULT_CUSTOMER_NAME = "Khách hàng";
    private static final String SESSION_CLEARED = "SESSION_CLEARED";

    private final ConcurrentMap<String, List<ChatMessage>> conversations = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> httpSessionToChatSession = new ConcurrentHashMap<>();

    public SupportChatBootstrapDto bootstrap(HttpSession httpSession) {
        String sessionId = resolveOrCreateSessionId(httpSession);
        syncHttpSessionBinding(httpSession.getId(), sessionId);
        return new SupportChatBootstrapDto(sessionId, getMessages(sessionId));
    }

    public void appendMessage(ChatMessage message) {
        ChatMessage normalizedMessage = normalizeMessage(message);
        conversations.compute(normalizedMessage.getSenderId(), (sessionId, existingMessages) -> {
            List<ChatMessage> messages = existingMessages == null ? new ArrayList<>() : new ArrayList<>(existingMessages);
            messages.add(normalizedMessage);
            return messages;
        });
    }

    public List<SupportChatSessionDto> getAllSessions() {
        return conversations.entrySet().stream()
                .map(entry -> {
                    List<ChatMessage> messages = List.copyOf(entry.getValue());
                    ChatMessage lastMessage = messages.isEmpty() ? null : messages.get(messages.size() - 1);
                    return new SupportChatSessionDto(
                            entry.getKey(),
                            resolveCustomerName(messages),
                            messages,
                            lastMessage);
                })
                .sorted(Comparator.comparing(
                        SupportChatSessionDto::lastMessage,
                        Comparator.nullsLast(Comparator.comparing(ChatMessage::getTimestamp, Comparator.nullsLast(Comparator.naturalOrder()))))
                        .reversed())
                .toList();
    }

    public SupportChatSessionDto getSession(String sessionId) {
        List<ChatMessage> messages = getMessages(sessionId);
        ChatMessage lastMessage = messages.isEmpty() ? null : messages.get(messages.size() - 1);
        return new SupportChatSessionDto(sessionId, resolveCustomerName(messages), messages, lastMessage);
    }

    public List<ChatMessage> getMessages(String sessionId) {
        return List.copyOf(conversations.getOrDefault(sessionId, List.of()));
    }

    public boolean isChatSessionActive(String sessionId) {
        return sessionId != null && !sessionId.isBlank() && conversations.containsKey(sessionId);
    }

    public void rebindHttpSession(HttpSession httpSession, String oldHttpSessionId) {
        Object existing = httpSession.getAttribute(CHAT_SESSION_KEY);
        if (!(existing instanceof String storedSessionId) || storedSessionId.isBlank()) {
            return;
        }

        if (oldHttpSessionId != null && !oldHttpSessionId.isBlank()) {
            httpSessionToChatSession.remove(oldHttpSessionId);
        }

        syncHttpSessionBinding(httpSession.getId(), storedSessionId);
    }

    public SupportChatSessionEventDto clearByHttpSessionId(String httpSessionId, String reason) {
        if (httpSessionId == null || httpSessionId.isBlank()) {
            return null;
        }

        String chatSessionId = httpSessionToChatSession.remove(httpSessionId);
        if (chatSessionId == null || chatSessionId.isBlank()) {
            return null;
        }

        conversations.remove(chatSessionId);
        return new SupportChatSessionEventDto(SESSION_CLEARED, chatSessionId, reason);
    }

    private String resolveOrCreateSessionId(HttpSession httpSession) {
        Object existing = httpSession.getAttribute(CHAT_SESSION_KEY);
        if (existing instanceof String storedSessionId && !storedSessionId.isBlank()) {
            conversations.putIfAbsent(storedSessionId, new ArrayList<>());
            return storedSessionId;
        }

        String newSessionId = UUID.randomUUID().toString();
        httpSession.setAttribute(CHAT_SESSION_KEY, newSessionId);
        conversations.putIfAbsent(newSessionId, new ArrayList<>());
        return newSessionId;
    }

    private ChatMessage normalizeMessage(ChatMessage message) {
        String senderName = message.getSenderName();
        if (senderName == null || senderName.isBlank()) {
            senderName = message.isAdmin() ? "Ho tro vien (Admin)" : DEFAULT_CUSTOMER_NAME;
        }

        return ChatMessage.builder()
                .senderId(message.getSenderId())
                .senderName(senderName)
                .content(message.getContent())
                .isAdmin(message.isAdmin())
                .timestamp(message.getTimestamp())
                .build();
    }

    private String resolveCustomerName(List<ChatMessage> messages) {
        return messages.stream()
                .filter(message -> !message.isAdmin())
                .map(ChatMessage::getSenderName)
                .filter(name -> name != null && !name.isBlank())
                .findFirst()
                .orElse(DEFAULT_CUSTOMER_NAME);
    }

    private void syncHttpSessionBinding(String httpSessionId, String chatSessionId) {
        if (httpSessionId == null || httpSessionId.isBlank() || chatSessionId == null || chatSessionId.isBlank()) {
            return;
        }
        httpSessionToChatSession.put(httpSessionId, chatSessionId);
    }
}
