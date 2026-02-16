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
@Table(name = "watches")
public class Watch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "watch_id")
    private Integer watchId;

    @Column(name = "watch_name", nullable = false, length = 200)
    private String watchName;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_percent")
    private Integer discountPercent = 0;

    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    @Column(name = "sold_count")
    private Integer soldCount = 0;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_date")
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "updated_date")
    private LocalDateTime updatedDate = LocalDateTime.now();

    // Relationships
    @ManyToOne
    @JoinColumn(name = "brand_id", nullable = false)
    private WatchBrand brand;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private WatchCategory category;

    @OneToMany(mappedBy = "watch", cascade = CascadeType.ALL)
    private List<WatchImage> images;

    @OneToMany(mappedBy = "watch", cascade = CascadeType.ALL)
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "watch", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;

    // Business method
    public BigDecimal getPriceAfterDiscount() {
        if (discountPercent != null && discountPercent > 0) {
            BigDecimal discount = price.multiply(BigDecimal.valueOf(discountPercent))
                    .divide(BigDecimal.valueOf(100));
            return price.subtract(discount);
        }
        return price;
    }
}
