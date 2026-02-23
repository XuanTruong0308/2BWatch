package boiz.shop._2BShop.respository;

import boiz.shop._2BShop.entity.BanLog;
import boiz.shop._2BShop.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BanLogRepository extends JpaRepository<BanLog, Integer> {
    @EntityGraph(attributePaths = {"user", "user.userRoles", "user.userRoles.role", "violationType"})
    List<BanLog> findByUserUserIdOrderByBanStartDateDesc(Integer userId);
    
    @EntityGraph(attributePaths = {"user", "user.userRoles", "user.userRoles.role", "violationType"})
    @Query("SELECT b FROM BanLog b WHERE b.user.userId = ?1 AND b.isActive = true AND b.banEndDate > CURRENT_TIMESTAMP")
    Optional<BanLog> findActiveBanByUserId(Integer userId);
    
    @EntityGraph(attributePaths = {"user", "user.userRoles", "user.userRoles.role", "violationType"})
    List<BanLog> findByIsActiveTrueOrderByBanStartDateDesc();
    
    // For BanAdminController
    @EntityGraph(attributePaths = {"user", "user.userRoles", "user.userRoles.role", "violationType"})
    List<BanLog> findByUserUserId(Integer userId);
    
    @EntityGraph(attributePaths = {"user", "user.userRoles", "user.userRoles.role", "violationType"})
    Page<BanLog> findByIsActive(Boolean isActive, Pageable pageable);
    
    @EntityGraph(attributePaths = {"user", "user.userRoles", "user.userRoles.role", "violationType"})
    List<BanLog> findByUserUserIdAndIsActiveTrue(Integer userId);
    
    @EntityGraph(attributePaths = {"user", "user.userRoles", "user.userRoles.role", "violationType"})
    List<BanLog> findByUserAndIsActive(User user, Boolean isActive);
    
    @Override
    @EntityGraph(attributePaths = {"user", "user.userRoles", "user.userRoles.role", "violationType"})
    Page<BanLog> findAll(Pageable pageable);
    
    @Override
    @EntityGraph(attributePaths = {"user", "user.userRoles", "user.userRoles.role", "violationType"})
    Optional<BanLog> findById(Integer id);
}
