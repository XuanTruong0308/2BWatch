package boiz.shop._2BShop.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import boiz.shop._2BShop.entity.Watch;
import boiz.shop._2BShop.respository.WatchRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import java.util.Optional;

@Service
public class WatchService {

    @Autowired
    private WatchRepository watchRepository;

    /**
     * Tìm tất cả sản phẩm active (cho user)
     */
    public Page<Watch> findActiveProducts(Pageable pageable) {
        return watchRepository.findByStockQuantityGreaterThan(0, pageable);
    }

    /**
     * Search theo tên hoặc brand
     */
    public Page<Watch> search(String keyword, Pageable pageable) {
        return watchRepository.findByWatchNameContainingOrBrandBrandNameContaining(
                keyword, keyword, pageable);
    }

    /**
     * Filter theo brand
     */
    public Page<Watch> findByBrand(String brand, Pageable pageable) {
        return watchRepository.findByBrandBrandName(brand, pageable);
    }

    /**
     * Filter theo category
     */
    public Page<Watch> findByCategory(String category, Pageable pageable) {
        return watchRepository.findByCategoryCategoryName(category, pageable);
    }

    /**
     * Filter theo khoảng giá
     */
    public Page<Watch> findByPriceRange(String priceRange, Pageable pageable) {
        BigDecimal minPrice = BigDecimal.ZERO;
        BigDecimal maxPrice = new BigDecimal("999999999");

        switch (priceRange) {
            case "under-1m":
                maxPrice = new BigDecimal("1000000");
                break;
            case "1m-3m":
                minPrice = new BigDecimal("1000000");
                maxPrice = new BigDecimal("3000000");
                break;
            case "3m-5m":
                minPrice = new BigDecimal("3000000");
                maxPrice = new BigDecimal("5000000");
                break;
            case "5m-10m":
                minPrice = new BigDecimal("5000000");
                maxPrice = new BigDecimal("10000000");
                break;
            case "over-10m":
                minPrice = new BigDecimal("10000000");
                break;
        }

        return watchRepository.findByPriceBetween(minPrice, maxPrice, pageable);
    }

    /**
     * Tìm sản phẩm theo ID
     */
    public Optional<Watch> findById(Integer id) {
        return watchRepository.findById(id);
    }

    /**
     * Sản phẩm mới nhất (cho trang chủ)
     */
    public List<Watch> findLatestWatches(int limit) {
        return watchRepository.findTop8ByOrderByCreatedDateDesc();
    }

    /**
     * Best sellers (top 3)
     */
    public List<Watch> getTop3BestSellers() {
        return watchRepository.findTop3ByOrderBySoldCountDesc();
    }

    /**
     * Sản phẩm mới nhất (top 3)
     */
    public List<Watch> getTop3Newest() {
        return watchRepository.findTop3ByIsActiveTrueOrderByCreatedDateDesc();
    }

    /**
     * Sản phẩm giảm giá nhiều nhất
     */
    public List<Watch> getTop3BiggestDiscount() {
        return watchRepository.findTop3ByOrderByDiscountPercentDesc();
    }

    /**
     * Sản phẩm liên quan (cùng brand hoặc category)
     */
    public List<Watch> findRelatedProducts(Watch currentWatch, int limit) {
        return watchRepository.findTop4ByBrandBrandNameOrCategoryCategoryNameAndWatchIdNot(
                currentWatch.getBrand().getBrandName(),
                currentWatch.getCategory().getCategoryName(),
                currentWatch.getWatchId());
    }

    /**
     * Sản phẩm mới nhất với pagination (trong vòng 3 ngày gần đây)
     */
    public Page<Watch> findNewestProducts(Pageable pageable) {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        return watchRepository.findByIsActiveTrueAndCreatedDateAfterOrderByCreatedDateDesc(threeDaysAgo, pageable);
    }

    /**
     * Sản phẩm giảm giá với pagination
     */
    public Page<Watch> findDiscountProducts(Pageable pageable) {
        return watchRepository.findByIsActiveTrueAndDiscountPercentGreaterThanOrderByDiscountPercentDesc(0, pageable);
    }
}