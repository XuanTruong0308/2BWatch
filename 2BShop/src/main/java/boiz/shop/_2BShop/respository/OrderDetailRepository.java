package boiz.shop._2BShop.respository;

import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    @EntityGraph(attributePaths = {"watch", "watch.images"})
    List<OrderDetail> findByOrderOrderId(Integer orderId);
    
    // Thêm method cho OrderService
    @EntityGraph(attributePaths = {"watch", "watch.images"})
    List<OrderDetail> findByOrder(Order order);
    
    // Admin methods
    /**
     * Check if watch exists in any order (for delete validation)
     */
    boolean existsByWatchWatchId(Integer watchId);
}
