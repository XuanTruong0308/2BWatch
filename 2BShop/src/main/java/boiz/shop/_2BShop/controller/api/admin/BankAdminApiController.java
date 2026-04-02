package boiz.shop._2BShop.controller.api.admin;

import boiz.shop._2BShop.controller.api.ApiDataMapper;
import boiz.shop._2BShop.entity.BankAccount;
import boiz.shop._2BShop.service.BankAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Admin Bank Account API")
@RestController
@RequestMapping("/api/v1/admin/bank-accounts")
public class BankAdminApiController {

    @Autowired
    private BankAccountService bankAccountService;

    @Operation(summary = "Get bank accounts")
    @GetMapping
    public ResponseEntity<Map<String, Object>> list() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("success", true);
            response.put("data", ApiDataMapper.mapBankAccounts(bankAccountService.getAllBankAccounts()));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Get bank account detail")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> detail(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            BankAccount bankAccount = bankAccountService.getBankAccount(id);
            response.put("success", true);
            response.put("data", ApiDataMapper.bankAccount(bankAccount));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Create bank account")
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody BankAccount bankAccount) {
        Map<String, Object> response = new HashMap<>();
        try {
            bankAccount.setCreatedAt(LocalDateTime.now());
            bankAccount.setUpdatedAt(LocalDateTime.now());

            BankAccount saved = bankAccountService.save(bankAccount);
            bankAccountService.generateAndSaveQrCode(saved);

            response.put("success", true);
            response.put("message", "Lưu thành công!");
            response.put("data", ApiDataMapper.bankAccount(saved));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Update bank account")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Integer id, @RequestBody BankAccount bankAccount) {
        Map<String, Object> response = new HashMap<>();
        try {
            BankAccount existing = bankAccountService.getBankAccount(id);

            existing.setBankName(bankAccount.getBankName());
            existing.setBankCode(bankAccount.getBankCode());
            existing.setAccountNumber(bankAccount.getAccountNumber());
            existing.setAccountHolder(bankAccount.getAccountHolder());
            existing.setIsActive(bankAccount.getIsActive());
            existing.setDisplayOrder(bankAccount.getDisplayOrder());
            existing.setUpdatedAt(LocalDateTime.now());

            bankAccountService.generateAndSaveQrCode(existing);
            BankAccount saved = bankAccountService.save(existing);

            response.put("success", true);
            response.put("message", "Cập nhật thành công!");
            response.put("data", ApiDataMapper.bankAccount(saved));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Delete bank account")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            bankAccountService.delete(id);
            response.put("success", true);
            response.put("message", "Xóa thành công!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
