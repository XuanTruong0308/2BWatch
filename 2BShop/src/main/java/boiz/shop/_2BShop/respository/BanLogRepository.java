package boiz.shop._2BShop.respository;

import boiz.shop._2BShop.entity.BanLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BanLogRepository extends JpaRepository<BanLog, Integer> {
    List<BanLog> findByUserUserIdOrderByBanStartDateDesc(Integer userId);
    
    @Query("SELECT b FROM BanLog b WHERE b.user.userId = ?1 AND b.isActive = true AND b.banEndDate > CURRENT_TIMESTAMP")
    Optional<BanLog> findActiveBanByUserId(Integer userId);
    
    List<BanLog> findByIsActiveTrueOrderByBanStartDateDesc();
    
    // For BanAdminController
    List<BanLog> findByUserUserId(Integer userId);
    Page<BanLog> findByIsActive(Boolean isActive, Pageable pageable);
    List<BanLog> findByUserUserIdAndIsActiveTrue(Integer userId);
}
