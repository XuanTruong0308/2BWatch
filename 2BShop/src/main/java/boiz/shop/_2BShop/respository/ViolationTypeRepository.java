package boiz.shop._2BShop.respository;

import boiz.shop._2BShop.entity.ViolationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ViolationTypeRepository extends JpaRepository<ViolationType, Integer> {
    Optional<ViolationType> findByTypeName(String typeName);
}
