package boiz.shop._2BShop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "violation_types")
public class ViolationType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "violation_type_id")
    private Integer violationTypeId;
    
    @Column(name = "type_name", unique = true, nullable = false, length = 50)
    private String typeName;
    
    @Column(length = 500)
    private String description;
    
    // Relationships
    @ToString.Exclude
    @OneToMany(mappedBy = "violationType", cascade = CascadeType.ALL)
    private List<BanLog> banLogs;
}
