package boiz.shop._2BShop.respository;

import boiz.shop._2BShop.entity.Watch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface WatchRepository extends JpaRepository<Watch, Integer> {
    // // Tìm theo brand
    // List<Watch> findByBrandBrandIdAndIsActiveTrue(Integer brandId);
    
    // // Tìm theo category
    // List<Watch> findByCategoryCategoryIdAndIsActiveTrue(Integer categoryId);
    
    // // Tìm theo tên
    // List<Watch> findByWatchNameContainingAndIsActiveTrue(String name);
    
    // // Top 3 bán chạy nhất
    // @Query("SELECT w FROM Watch w WHERE w.isActive = true ORDER BY w.soldCount DESC")
    // List<Watch> findTop3BestSellers();
    
    // // Top 3 mới nhất
    // @Query("SELECT w FROM Watch w WHERE w.isActive = true ORDER BY w.createdDate DESC")
    // List<Watch> findTop3Newest();
    
    // // Top 3 giảm giá sâu nhất
    // @Query("SELECT w FROM Watch w WHERE w.isActive = true AND w.discountPercent > 0 ORDER BY w.discountPercent DESC")
    // List<Watch> findTop3BiggestDiscount();
    
    // // Tất cả sản phẩm active
    // List<Watch> findByIsActiveTrueOrderByCreatedDateDesc();

    List<Watch> findTop3ByIsActiveTrueOrderBySoldCountDesc();

    List<Watch> findTop3ByIsActiveTrueOrderByCreatedDateDesc();

    List<Watch> findTop3ByIsActiveTrueAndDiscountPercentGreaterThanOrderByDiscountPercentDesc(Integer minDiscount);

    List<Watch> findByIsActiveTrueOrderByCreatedDateDesc();
    
    // Search by name
    List<Watch> findByWatchNameContainingIgnoreCaseAndIsActiveTrue(String name);
    
    // Find by category
    List<Watch> findByCategoryCategoryIdAndIsActiveTrueOrderByCreatedDateDesc(Integer categoryId);
    
    // Find by brand
    List<Watch> findByBrandBrandIdAndIsActiveTrueOrderByCreatedDateDesc(Integer brandId);
    
    // Find related products by category (exclude current product)
    List<Watch> findTop4ByCategoryCategoryIdAndIsActiveTrueAndWatchIdNot(Integer categoryId, Integer watchId);
    
    // ========================================
    // Methods for WatchService and AdminController
    // ========================================
    
    // Pagination support
    Page<Watch> findByStockQuantityGreaterThan(Integer quantity, Pageable pageable);
    Page<Watch> findByStockQuantity(Integer quantity, Pageable pageable);
    Page<Watch> findByWatchNameContainingOrBrandBrandNameContaining(String name, String brand, Pageable pageable);
    Page<Watch> findByBrandBrandName(String brand, Pageable pageable);
    Page<Watch> findByCategoryCategoryName(String category, Pageable pageable);
    Page<Watch> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    // Top products
    List<Watch> findTop8ByOrderByCreatedDateDesc();
    List<Watch> findTop3ByOrderBySoldCountDesc();
    List<Watch> findTop3ByOrderByDiscountPercentDesc();
    List<Watch> findTop4ByBrandBrandNameOrCategoryCategoryNameAndWatchIdNot(String brand, String category, Integer watchId);
    
    // Count methods
    Long countByIsActiveTrue();
    
    // Admin methods - Pagination with filters
    Page<Watch> findByWatchNameContainingIgnoreCase(String keyword, Pageable pageable);
    Page<Watch> findByBrandBrandId(Integer brandId, Pageable pageable);
    Page<Watch> findByCategoryCategoryId(Integer categoryId, Pageable pageable);
    Page<Watch> findByIsActive(Boolean isActive, Pageable pageable);

}
