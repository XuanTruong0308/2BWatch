package boiz.shop._2BShop.api.controller.admin;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import boiz.shop._2BShop.api.dto.chat.SupportChatSessionDto;
import boiz.shop._2BShop.api.response.ApiResponse;
import boiz.shop._2BShop.service.SupportChatSessionService;

@RestController
@RequestMapping("/api/v1/admin/support-chat")
public class AdminSupportChatApiController {

    private final SupportChatSessionService supportChatSessionService;

    public AdminSupportChatApiController(SupportChatSessionService supportChatSessionService) {
        this.supportChatSessionService = supportChatSessionService;
    }

    @GetMapping("/sessions")
    public ApiResponse<List<SupportChatSessionDto>> sessions() {
        return ApiResponse.success(supportChatSessionService.getAllSessions());
    }

    @GetMapping("/sessions/{sessionId}")
    public ApiResponse<SupportChatSessionDto> session(@PathVariable String sessionId) {
        return ApiResponse.success(supportChatSessionService.getSession(sessionId));
    }
}
