package boiz.shop._2BShop.respository;

import boiz.shop._2BShop.entity.PaymentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Integer> {
    @EntityGraph(attributePaths = {"order", "order.user", "paymentMethod"})
    List<PaymentTransaction> findByOrderOrderId(Integer orderId);
    
    @EntityGraph(attributePaths = {"order", "order.user", "paymentMethod"})
    Optional<PaymentTransaction> findByTransactionCode(String transactionCode);
    
    // For PaymentAdminController
    @EntityGraph(attributePaths = {"order", "order.user", "paymentMethod"})
    Page<PaymentTransaction> findByStatus(String status, Pageable pageable);
    
    @EntityGraph(attributePaths = {"order", "order.user", "paymentMethod"})
    Page<PaymentTransaction> findByPaymentMethodPaymentMethodId(Integer methodId, Pageable pageable);
    
    @Override
    @EntityGraph(attributePaths = {"order", "order.user", "paymentMethod"})
    Page<PaymentTransaction> findAll(Pageable pageable);
    
    @Override
    @EntityGraph(attributePaths = {"order", "order.user", "paymentMethod"})
    Optional<PaymentTransaction> findById(Integer id);
}
