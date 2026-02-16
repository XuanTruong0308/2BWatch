package boiz.shop._2BShop.controller.admin;

import boiz.shop._2BShop.entity.BankAccount;
import boiz.shop._2BShop.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/bank-accounts")
public class BankAdminController {

    @Autowired
    private BankAccountService bankAccountService;

    @GetMapping
    public String listBankAccounts(Model model) {
        model.addAttribute("bankAccounts", bankAccountService.getAllBankAccounts());
        return "admin/bank-accounts";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("bankAccount", new BankAccount());
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
