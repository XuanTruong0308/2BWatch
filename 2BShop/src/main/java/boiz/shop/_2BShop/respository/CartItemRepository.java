package boiz.shop._2BShop.respository;

import boiz.shop._2BShop.entity.Cart;
import boiz.shop._2BShop.entity.CartItem;
import boiz.shop._2BShop.entity.Watch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    List<CartItem> findByCartCartId(Integer cartId);
    Optional<CartItem> findByCartCartIdAndWatchWatchId(Integer cartId, Integer watchId);
    void deleteByCartCartId(Integer cartId);
    
    // Thêm các methods cho CartService
    List<CartItem> findByCart(Cart cart);
    
    Optional<CartItem> findByCartAndWatch(Cart cart, Watch watch);
    
    @Transactional
    void deleteByCart(Cart cart);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.watch.watchId = :watchId")
    void deleteByWatchWatchId(@Param("watchId") Integer watchId);
}
