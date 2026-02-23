package boiz.shop._2BShop.controller.admin;

import boiz.shop._2BShop.entity.BankAccount;
import boiz.shop._2BShop.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/bank-accounts")
public class BankAdminController {

    @Autowired
    private BankAccountService bankAccountService;

    /**
     * Hiển thị trang quản lý ngân hàng
     */
    @GetMapping
    public String listBankAccounts(Model model) {
        model.addAttribute("bankAccounts", bankAccountService.getAllBankAccounts());
        model.addAttribute("activeMenu", "bank-accounts");
        return "admin/bank-accounts";
    }

    /**
     * API: Lấy thông tin bank theo ID
     */
    @GetMapping("/api/{id}")
    @ResponseBody
    public BankAccount getBankAccountById(@PathVariable Integer id) {
        return bankAccountService.getBankAccount(id);
    }

    /**
     * API: Lưu bank account (Add/Edit)
     */
    @PostMapping("/api/save")
    @ResponseBody
    public Map<String, Object> saveBankAccount(@RequestBody BankAccount bankAccount) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Set timestamps
            if (bankAccount.getBankAccountId() == null) {
                bankAccount.setCreatedAt(LocalDateTime.now());
            }
            bankAccount.setUpdatedAt(LocalDateTime.now());

            // Save first to get ID
            BankAccount saved = bankAccountService.save(bankAccount);

            // Generate QR Code
            bankAccountService.generateAndSaveQrCode(saved);

            response.put("success", true);
            response.put("message", "Lưu thành công!");
            response.put("data", saved);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }

    /**
     * API: Xóa bank account
     */
    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteBankAccount(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            bankAccountService.delete(id);
            response.put("success", true);
            response.put("message", "Xóa thành công!");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        
        return response;
    }

    // ===== OLD ROUTES (Keep for compatibility, but can be removed later) =====
    
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("bankAccount", new BankAccount());
        model.addAttribute("activeMenu", "bank-accounts");
        return "admin/bank-account-form";
    }

    @PostMapping("/add")
    public String addBankAccount(@ModelAttribute BankAccount bankAccount, RedirectAttributes redirectAttributes) {
        try {
            // Save first to get ID
            BankAccount savedBank = bankAccountService.save(bankAccount);

            // Auto generate QR
            bankAccountService.generateAndSaveQrCode(savedBank);

            redirectAttributes.addFlashAttribute("success", "Thêm tài khoản ngân hàng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/bank-accounts/add";
        }
        return "redirect:/admin/bank-accounts";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        try {
            BankAccount bankAccount = bankAccountService.getBankAccount(id);
            model.addAttribute("bankAccount", bankAccount);
            model.addAttribute("activeMenu", "bank-accounts");
            return "admin/bank-account-form";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy tài khoản!");
            return "redirect:/admin/bank-accounts";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateBankAccount(@PathVariable Integer id, @ModelAttribute BankAccount bankAccount,
            RedirectAttributes redirectAttributes) {
        try {
            BankAccount existingBank = bankAccountService.getBankAccount(id);

            // Update fields
            existingBank.setBankName(bankAccount.getBankName());
            existingBank.setBankCode(bankAccount.getBankCode());
            existingBank.setAccountNumber(bankAccount.getAccountNumber());
            existingBank.setAccountHolder(bankAccount.getAccountHolder());
            existingBank.setIsActive(bankAccount.getIsActive());
            existingBank.setDisplayOrder(bankAccount.getDisplayOrder());

            // If details changed, maybe regenerate QR?
            // For now, let's regenerate every time or check if critical fields changed.
            // Safe to regenerate.
            bankAccountService.generateAndSaveQrCode(existingBank);

            bankAccountService.save(existingBank);
            redirectAttributes.addFlashAttribute("success", "Cập nhật thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/bank-accounts/edit/" + id;
        }
        return "redirect:/admin/bank-accounts";
    }

    @GetMapping("/delete/{id}")
    public String deleteBankAccount(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            bankAccountService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa: " + e.getMessage());
        }
        return "redirect:/admin/bank-accounts";
    }
}
