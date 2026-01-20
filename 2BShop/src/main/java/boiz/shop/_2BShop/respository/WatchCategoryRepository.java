package boiz.shop._2BShop.respository;

import boiz.shop._2BShop.entity.WatchCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WatchCategoryRepository extends JpaRepository<WatchCategory, Integer> {
    List<WatchCategory> findByIsActiveTrueOrderByCategoryName();
}
