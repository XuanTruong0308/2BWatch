package boiz.shop._2BShop.respository;

import boiz.shop._2BShop.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.userRoles ur WHERE ur.role.roleName = :roleName")
    Long countByRoleName(@Param("roleName") String roleName);

    // Admin methods
    /**
     * Search users by username, email, or full name
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.email LIKE %:keyword% OR u.fullName LIKE %:keyword%")
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find by isEnabled (active status)
     */
    Page<User> findByIsEnabled(Boolean isEnabled, Pageable pageable);
}
