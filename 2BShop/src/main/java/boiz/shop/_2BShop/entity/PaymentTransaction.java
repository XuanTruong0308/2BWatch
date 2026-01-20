package boiz.shop._2BShop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Integer transactionId;
    
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @Column(name = "transaction_code", length = 100)
    private String transactionCode;
    
    @ManyToOne
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethod paymentMethod;
    
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;
    
    @Column(length = 20)
    private String status = "PENDING"; // PENDING, SUCCESS, FAILED
    
    @Column(name = "transaction_date")
    private LocalDateTime transactionDate = LocalDateTime.now();
    
    @Column(name = "response_data", columnDefinition = "NVARCHAR(MAX)")
    private String responseData;
}
