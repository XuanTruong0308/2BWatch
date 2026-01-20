package boiz.shop._2BShop.controller.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import boiz.shop._2BShop.entity.PaymentMethod;
import boiz.shop._2BShop.entity.PaymentTransaction;
import boiz.shop._2BShop.respository.PaymentMethodRepository;
import boiz.shop._2BShop.respository.PaymentTransactionRepository;

/**
 * Admin Controller for Payment Management
 * - Quản lý Payment Methods (COD, Bank Transfer, VNPay, etc.)
 * - Xem Payment Transactions history
 * - Thống kê theo payment method
 */
@Controller
@RequestMapping("/admin/payments")
public class PaymentAdminController {
    
    @Autowired
    private PaymentMethodRepository paymentMethodRepo;
    
    @Autowired
    private PaymentTransactionRepository paymentTransactionRepo;
    
    /**
     * List all payment methods
     * URL: /admin/payments/methods
     */
    @GetMapping("/methods")
    public String listPaymentMethods(Model model) {
        List<PaymentMethod> methods = paymentMethodRepo.findAll();
        model.addAttribute("paymentMethods", methods);
        return "admin/payment-methods";
    }
    
    /**
     * Show form to add new payment method
     * URL: /admin/payments/methods/new
     */
    @GetMapping("/methods/new")
    public String newPaymentMethodForm(Model model) {
        PaymentMethod method = new PaymentMethod();
        method.setIsActive(true);
        model.addAttribute("paymentMethod", method);
        model.addAttribute("isEdit", false);
        return "admin/payment-method-form";
    }
    
    /**
     * Show form to edit payment method
     * URL: /admin/payments/methods/edit/{id}
     */
    @GetMapping("/methods/edit/{id}")
    public String editPaymentMethodForm(@PathVariable Integer id, Model model) {
        PaymentMethod method = paymentMethodRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Payment method not found with id: " + id));
        model.addAttribute("paymentMethod", method);
        model.addAttribute("isEdit", true);
        return "admin/payment-method-form";
    }
    
    /**
     * Save payment method (create or update)
     * URL: POST /admin/payments/methods/save
     */
    @PostMapping("/methods/save")
    public String savePaymentMethod(
        @ModelAttribute PaymentMethod paymentMethod,
        RedirectAttributes redirectAttributes
    ) {
        try {
            if (paymentMethod.getPaymentMethodId() == null) {
                paymentMethod.setCreatedDate(LocalDateTime.now());
            }
            paymentMethod.setUpdatedDate(LocalDateTime.now());
            
            paymentMethodRepo.save(paymentMethod);
            
            redirectAttributes.addFlashAttribute("success", "Lưu phương thức thanh toán thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "redirect:/admin/payments/methods";
    }
    
    /**
     * Toggle payment method active status
     * URL: POST /admin/payments/methods/toggle-active/{id}
     */
    @PostMapping("/methods/toggle-active/{id}")
    public String togglePaymentMethodActive(
        @PathVariable Integer id,
        RedirectAttributes redirectAttributes
    ) {
        try {
            PaymentMethod method = paymentMethodRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment method not found"));
            
            method.setIsActive(!method.getIsActive());
            method.setUpdatedDate(LocalDateTime.now());
            paymentMethodRepo.save(method);
            
            String status = method.getIsActive() ? "Active" : "Inactive";
            redirectAttributes.addFlashAttribute("success", "Đã chuyển phương thức thanh toán sang " + status);
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/payments/methods";
    }
    
    /**
     * List all payment transactions with pagination
     * URL: /admin/payments/transactions
     */
    @GetMapping("/transactions")
    public String listTransactions(
        @RequestParam(required = false) String status,
        @RequestParam(required = false) Integer methodId,
        @RequestParam(defaultValue = "0") int page,
        Model model
    ) {
        Pageable pageable = PageRequest.of(page, 50, Sort.by("transactionDate").descending());
        
        Page<PaymentTransaction> transactions;
        
        if (status != null && !status.isEmpty()) {
            transactions = paymentTransactionRepo.findByStatus(status, pageable);
        } else if (methodId != null) {
            transactions = paymentTransactionRepo.findByPaymentMethodPaymentMethodId(methodId, pageable);
        } else {
            transactions = paymentTransactionRepo.findAll(pageable);
        }
        
        // Calculate total amount
        BigDecimal totalAmount = transactions.getContent().stream()
            .map(PaymentTransaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        model.addAttribute("transactions", transactions);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("paymentMethods", paymentMethodRepo.findAll());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedMethodId", methodId);
        
        return "admin/payment-transactions";
    }
    
    /**
     * View transaction detail
     * URL: /admin/payments/transactions/{id}
     */
    @GetMapping("/transactions/{id}")
    public String transactionDetail(@PathVariable Integer id, Model model) {
        PaymentTransaction transaction = paymentTransactionRepo.findById(id)
            .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
        
        model.addAttribute("transaction", transaction);
        return "admin/payment-transaction-detail";
    }
}
