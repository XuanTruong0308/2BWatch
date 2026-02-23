package boiz.shop._2BShop.respository;

import boiz.shop._2BShop.entity.WatchBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WatchBrandRepository extends JpaRepository<WatchBrand, Integer> {
    @EntityGraph(attributePaths = {"watches"})
    List<WatchBrand> findByIsActiveTrueOrderByBrandName();
    
    @Override
    @EntityGraph(attributePaths = {"watches"})
    List<WatchBrand> findAll();
    
    @Override
    @EntityGraph(attributePaths = {"watches"})
    Optional<WatchBrand> findById(Integer id);
}
