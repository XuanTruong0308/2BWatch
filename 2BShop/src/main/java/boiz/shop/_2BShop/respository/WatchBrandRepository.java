package boiz.shop._2BShop.respository;

import boiz.shop._2BShop.entity.WatchBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WatchBrandRepository extends JpaRepository<WatchBrand, Integer> {
    List<WatchBrand> findByIsActiveTrueOrderByBrandName();
}
