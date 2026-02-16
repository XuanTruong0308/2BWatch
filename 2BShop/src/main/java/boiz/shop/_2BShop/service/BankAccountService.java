package boiz.shop._2BShop.service;

import boiz.shop._2BShop.entity.BankAccount;
import boiz.shop._2BShop.respository.BankAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BankAccountService {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    /**
     * Get all active bank accounts for checkout display
     */
    public List<BankAccount> getActiveBankAccounts() {
        return bankAccountRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    /**
     * Get bank account by ID
     */
    public BankAccount getBankAccount(Integer id) {
        return bankAccountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bank account not found"));
    }

    /**
     * Get all bank accounts (for admin)
     */
    public List<BankAccount> getAllBankAccounts() {
        return bankAccountRepository.findAll();
    }

    /**
     * Save bank account
     */
    public BankAccount save(BankAccount bankAccount) {
        return bankAccountRepository.save(bankAccount);
    }

    /**
     * Delete bank account
     */
    public void delete(Integer id) {
        bankAccountRepository.deleteById(id);
    }

    /**
     * Generate QR Code from VietQR and save to disk
     */
    public void generateAndSaveQrCode(BankAccount bankAccount) {
        try {
            // 1. Check if ID exists (must be saved first)
            if (bankAccount.getBankAccountId() == null) {
                bankAccount = bankAccountRepository.save(bankAccount);
            }

            // 2. Construct API URL
            String apiUrl = String.format(
                    "https://img.vietqr.io/image/%s-%s-compact2.png?addInfo=Thanh toan don hang&accountName=%s",
                    bankAccount.getBankCode(),
                    bankAccount.getAccountNumber(),
                    bankAccount.getAccountHolder().replace(" ", "%20"));

            // 3. Download Image
            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            byte[] imageBytes = restTemplate.getForObject(apiUrl, byte[].class);

            if (imageBytes != null && imageBytes.length > 0) {
                // 4. Prepare directory
                String uploadDir = "C:/uploads/bshop/"; // Hardcoded for now based on app.properties, can be injected
                java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir, "banking");
                if (!java.nio.file.Files.exists(uploadPath)) {
                    java.nio.file.Files.createDirectories(uploadPath);
                }

                // 5. Save file: banking/bank_{id}.png
                String fileName = "bank_" + bankAccount.getBankAccountId() + ".png";
                java.nio.file.Path filePath = uploadPath.resolve(fileName);
                java.nio.file.Files.write(filePath, imageBytes);

                // 6. Update entity
                bankAccount.setQrImageUrl("/uploads/banking/" + fileName);
                bankAccountRepository.save(bankAccount);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback: Do nothing, existing logic will use API URL
        }
    }
}
