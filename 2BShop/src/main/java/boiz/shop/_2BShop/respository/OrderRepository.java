package boiz.shop._2BShop.respository;

import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
        List<Order> findByUserUserIdOrderByOrderDateDesc(Integer userId);

        Page<Order> findByUserUserIdOrderByOrderDateDesc(Integer userId, Pageable pageable);

        Page<Order> findByUserUserIdAndOrderStatusOrderByOrderDateDesc(Integer userId, String status,
                        Pageable pageable);

        List<Order> findByOrderStatusOrderByOrderDateDesc(String status);

        List<Order> findAllByOrderByOrderDateDesc();

        @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.orderStatus = :status")
        BigDecimal sumTotalAmountByStatus(@Param("status") String status);

        @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate AND o.orderStatus IN :statuses")
        BigDecimal sumTotalAmountByStatus(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("statuses") List<String> statuses);

        @Query("SELECT COUNT(o) FROM Order o WHERE o.orderStatus = :status")
        Long countByStatus(@Param("status") String status);

        List<Order> findTop10ByOrderByOrderDateDesc();

        // Thêm các method cho OrderService
        Page<Order> findByOrderStatus(String status, Pageable pageable);

        List<Order> findByUserOrderByOrderDateDesc(User user);

        // Method để tìm orders trong khoảng thời gian
        List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

        Page<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

        // Methods cho DashboardService và Admin

        /**
         * Tính tổng doanh thu theo date range và status
         */
        @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate AND o.orderStatus IN :statuses")
        BigDecimal sumTotalAmountByDateRangeAndStatus(
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("statuses") List<String> statuses);

        /**
         * Đếm orders theo status (cho dashboard stats)
         */
        Long countByOrderStatus(String status);

        /**
         * Tìm orders với status và date range
         */
        Page<Order> findByOrderStatusAndOrderDateBetween(
                        String status,
                        LocalDateTime startDate,
                        LocalDateTime endDate,
                        Pageable pageable);

        /**
         * Search orders by receiver name or phone
         */
        @Query("SELECT o FROM Order o WHERE o.receiverName LIKE %:keyword% OR o.shippingPhone LIKE %:keyword%")
        Page<Order> searchOrders(@Param("keyword") String keyword, Pageable pageable);

        /**
         * Đếm orders của user
         */
        Long countByUserUserId(Integer userId);

        /**
         * Tính tổng số tiền đã mua của user
         */
        @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.user.userId = :userId AND o.orderStatus IN ('DELIVERED', 'COMPLETED')")
        BigDecimal sumTotalAmountByUserUserId(@Param("userId") Integer userId);

        /**
         * Tìm orders của user (cho UserAdminController)
         */
        List<Order> findByUserUserId(Integer userId);
}
