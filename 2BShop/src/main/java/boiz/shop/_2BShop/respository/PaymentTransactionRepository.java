package boiz.shop._2BShop.respository;

import boiz.shop._2BShop.entity.PaymentTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Integer> {
    List<PaymentTransaction> findByOrderOrderId(Integer orderId);
    Optional<PaymentTransaction> findByTransactionCode(String transactionCode);
    
    // For PaymentAdminController
    Page<PaymentTransaction> findByStatus(String status, Pageable pageable);
    Page<PaymentTransaction> findByPaymentMethodPaymentMethodId(Integer methodId, Pageable pageable);
}
