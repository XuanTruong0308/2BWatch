package boiz.shop._2BShop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ban_logs")
public class BanLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ban_log_id")
    private Integer banLogId;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "violation_type_id", nullable = false)
    private ViolationType violationType;
    
    @Column(name = "violation_count", nullable = false)
    private Integer violationCount;
    
    @Column(name = "ban_duration_minutes")
    private Integer banDurationMinutes;
    
    @Column(name = "ban_start_date")
    private LocalDateTime banStartDate = LocalDateTime.now();
    
    @Column(name = "ban_end_date")
    private LocalDateTime banEndDate;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(length = 500)
    private String reason;
}
