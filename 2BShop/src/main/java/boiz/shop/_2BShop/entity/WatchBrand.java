package boiz.shop._2BShop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "watch_brands")
public class WatchBrand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_id")
    private Integer brandId;
    
    @Column(name = "brand_name", unique = true, nullable = false, length = 50)
    private String brandName;
    
    @Column(length = 500)
    private String description;
    
    @Column(name = "logo_url")
    private String logoUrl;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // Relationships
    @ToString.Exclude
    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL)
    private List<Watch> watches;
}
