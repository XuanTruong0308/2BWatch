package boiz.shop._2BShop.respository;

import boiz.shop._2BShop.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Integer> {
    List<PaymentMethod> findByIsActiveTrueOrderByMethodName();
    Optional<PaymentMethod> findByMethodName(String methodName);
}
