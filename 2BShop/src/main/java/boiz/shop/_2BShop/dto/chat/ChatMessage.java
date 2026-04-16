package boiz.shop._2BShop.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String senderId;
    private String senderName;
    private String content;
    
    @JsonProperty("isAdmin")
    private boolean isAdmin;
    
    private LocalDateTime timestamp;
}
