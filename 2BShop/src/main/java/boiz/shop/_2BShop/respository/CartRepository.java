package boiz.shop._2BShop.respository;

import boiz.shop._2BShop.entity.Cart;
import boiz.shop._2BShop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByUserUserId(Integer userId);
    
    // Thêm method cho CartService
    Optional<Cart> findByUser(User user);
}
