package boiz.shop._2BShop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;
    
    @Column(name = "shipping_phone", nullable = false, length = 20)
    private String shippingPhone;
    
    @Column(name = "receiver_name", nullable = false, length = 100)
    private String receiverName;
    
    @Column(name = "order_status", length = 20)
    private String orderStatus = "PENDING"; // PENDING, CONFIRMED, SHIPPING, DELIVERED, COMPLETED, CANCELLED
    
    @ManyToOne
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethod paymentMethod;
    
    @Column(name = "order_date")
    private LocalDateTime orderDate = LocalDateTime.now();
    
    @Column(name = "updated_date")
    private LocalDateTime updatedDate = LocalDateTime.now();
    
    @Column(length = 500)
    private String notes;
    
    // Relationships
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<PaymentTransaction> paymentTransactions;
}
