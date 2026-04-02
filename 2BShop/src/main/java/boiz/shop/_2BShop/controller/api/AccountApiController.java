package boiz.shop._2BShop.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Account API")
@RestController
@RequestMapping("/api/v1/account")
public class AccountApiController {

    @Operation(summary = "Account entry point")
    @GetMapping
    public ResponseEntity<Map<String, Object>> account() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("redirectTo", "/profile");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Legacy account orders route")
    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> orders() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("redirectTo", "/orders");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Legacy account change password route")
    @GetMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("redirectTo", "/profile");
        return ResponseEntity.ok(response);
    }
}
