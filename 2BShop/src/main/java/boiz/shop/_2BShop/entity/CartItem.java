package boiz.shop._2BShop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Integer cartItemId;
    
    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;
    
    @ManyToOne
    @JoinColumn(name = "watch_id", nullable = false)
    private Watch watch;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(precision = 18, scale = 2)
    private BigDecimal price;
    
    @Column(name = "added_date")
    private LocalDateTime addedDate = LocalDateTime.now();
}
