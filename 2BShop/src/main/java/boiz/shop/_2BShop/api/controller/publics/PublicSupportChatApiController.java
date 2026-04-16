package boiz.shop._2BShop.api.controller.publics;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import boiz.shop._2BShop.api.dto.chat.SupportChatBootstrapDto;
import boiz.shop._2BShop.api.response.ApiResponse;
import boiz.shop._2BShop.service.SupportChatSessionService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/v1/public/support-chat")
public class PublicSupportChatApiController {

    private final SupportChatSessionService supportChatSessionService;

    public PublicSupportChatApiController(SupportChatSessionService supportChatSessionService) {
        this.supportChatSessionService = supportChatSessionService;
    }

    @GetMapping("/session")
    public ApiResponse<SupportChatBootstrapDto> bootstrap(HttpSession httpSession) {
        return ApiResponse.success(supportChatSessionService.bootstrap(httpSession));
    }
}
