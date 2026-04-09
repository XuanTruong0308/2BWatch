package boiz.shop._2BShop.api.controller.admin;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import boiz.shop._2BShop.api.mapper.ApiMapper;
import boiz.shop._2BShop.api.request.BankAccountUpsertRequest;
import boiz.shop._2BShop.api.response.ApiResponse;
import boiz.shop._2BShop.entity.BankAccount;
import boiz.shop._2BShop.service.BankAccountService;

@RestController
@RequestMapping("/api/v1/admin/bank-accounts")
public class AdminBankAccountApiController {

    private final BankAccountService bankAccountService;
    private final ApiMapper apiMapper;

    public AdminBankAccountApiController(BankAccountService bankAccountService, ApiMapper apiMapper) {
        this.bankAccountService = bankAccountService;
        this.apiMapper = apiMapper;
    }

    @GetMapping
    public ApiResponse<List<?>> list() {
        return ApiResponse.success(bankAccountService.getAllBankAccounts().stream().map(apiMapper::toBankAccountDto).toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<?> detail(@PathVariable Integer id) {
        return ApiResponse.success(apiMapper.toBankAccountDto(bankAccountService.getBankAccount(id)));
    }

    @PostMapping
    public ApiResponse<?> create(@RequestBody BankAccountUpsertRequest request) {
        BankAccount bankAccount = new BankAccount();
        apply(bankAccount, request);
        bankAccount.setCreatedAt(LocalDateTime.now());
        bankAccount.setUpdatedAt(LocalDateTime.now());
        BankAccount saved = bankAccountService.save(bankAccount);
        bankAccountService.generateAndSaveQrCode(saved);
        return ApiResponse.success("Thêm tài khoản ngân hàng thành công", apiMapper.toBankAccountDto(bankAccountService.getBankAccount(saved.getBankAccountId())));
    }

    @PutMapping("/{id}")
    public ApiResponse<?> update(@PathVariable Integer id, @RequestBody BankAccountUpsertRequest request) {
        BankAccount bankAccount = bankAccountService.getBankAccount(id);
        apply(bankAccount, request);
        bankAccount.setUpdatedAt(LocalDateTime.now());
        BankAccount saved = bankAccountService.save(bankAccount);
        bankAccountService.generateAndSaveQrCode(saved);
        return ApiResponse.success("Cập nhật tài khoản ngân hàng thành công", apiMapper.toBankAccountDto(bankAccountService.getBankAccount(saved.getBankAccountId())));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Integer id) {
        bankAccountService.delete(id);
        return ApiResponse.success("Xóa tài khoản ngân hàng thành công", null);
    }

    private void apply(BankAccount bankAccount, BankAccountUpsertRequest request) {
        bankAccount.setBankName(request.bankName());
        bankAccount.setBankCode(request.bankCode());
        bankAccount.setAccountNumber(request.accountNumber());
        bankAccount.setAccountHolder(request.accountHolder());
        bankAccount.setIsActive(request.active() == null ? true : request.active());
        bankAccount.setDisplayOrder(request.displayOrder() == null ? 0 : request.displayOrder());
    }
}
