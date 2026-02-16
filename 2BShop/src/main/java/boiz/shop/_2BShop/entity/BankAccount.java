package boiz.shop._2BShop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bank_accounts")
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bank_account_id")
    private Integer bankAccountId;

    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Column(name = "bank_code", nullable = false, length = 20)
    private String bankCode; // MB, VCB, etc.

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber;

    @Column(name = "account_holder", nullable = false, length = 200)
    private String accountHolder;

    @Column(name = "qr_image_url", length = 500)
    private String qrImageUrl; // Path to uploaded QR code image

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "display_order")
    private Integer displayOrder = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * Get formatted bank info for display
     */
    public String getFormattedInfo() {
        return String.format("%s - %s - %s", bankName, accountNumber, accountHolder);
    }

    /**
     * Get QR Code URL.
     * Use uploaded image if available, otherwise generate VietQR API URL.
     * Format: https://img.vietqr.io/image/<BANK_CODE>-<ACCOUNT_NO>-<TEMPLATE>.png
     */
    public String getQrCodeUrl() {
        if (qrImageUrl != null && !qrImageUrl.isEmpty()) {
            return qrImageUrl;
        }
        // Fallback to VietQR
        // Template: compact, compact2, qr_only, print
        return String.format(
                "https://img.vietqr.io/image/%s-%s-compact2.jpg?addInfo=Thanh toan don hang&accountName=%s",
                bankCode, accountNumber, accountHolder.replace(" ", "%20"));
    }
}
