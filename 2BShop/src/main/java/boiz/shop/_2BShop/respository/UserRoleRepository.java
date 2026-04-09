package boiz.shop._2BShop.respository;

import boiz.shop._2BShop.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {
    @Transactional
    void deleteByUserUserId(Integer userId);
}
