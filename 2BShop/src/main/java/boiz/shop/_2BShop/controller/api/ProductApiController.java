package boiz.shop._2BShop.controller.api;

import boiz.shop._2BShop.entity.Watch;
import boiz.shop._2BShop.service.WatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Product API")
@RestController
@RequestMapping("/api/v1/products")
public class ProductApiController {

        @Autowired
        private WatchService watchService;

    @Operation(summary = "Get products with paging/filter")
    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false) String sortBy) {
                Map<String, Object> response = new HashMap<>();
                try {
                        Pageable pageable;
                        if ("price-asc".equals(sortBy)) {
                                pageable = PageRequest.of(page, size, Sort.by("price").ascending());
                        } else if ("price-desc".equals(sortBy)) {
                                pageable = PageRequest.of(page, size, Sort.by("price").descending());
                        } else {
                                pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
                        }

                        Page<Watch> watchPage;
                        String pageTitle = "Tất cả sản phẩm";

                        if (search != null && !search.trim().isEmpty()) {
                                watchPage = watchService.search(search, pageable);
                                pageTitle = "Tìm kiếm: " + search;
                        } else if (category != null && !category.trim().isEmpty()) {
                                watchPage = watchService.findByCategory(category, pageable);
                                pageTitle = "Danh mục: " + category;
                        } else if (brand != null && !brand.trim().isEmpty()) {
                                watchPage = watchService.findByBrand(brand, pageable);
                                pageTitle = "Thương hiệu: " + brand;
                        } else if (priceRange != null && !priceRange.trim().isEmpty()) {
                                watchPage = watchService.findByPriceRange(priceRange, pageable);
                                pageTitle = "Khoảng giá: " + getPriceRangeLabel(priceRange);
                        } else {
                                watchPage = watchService.findActiveProducts(pageable);
                        }

                        Map<String, Object> data = new HashMap<>();
                        data.put("items", ApiDataMapper.mapWatches(watchPage.getContent()));
                        data.put("page", ApiDataMapper.pageInfo(watchPage));
                        data.put("pageTitle", pageTitle);
                            Map<String, Object> filters = new HashMap<>();
                            filters.put("search", search);
                            filters.put("category", category);
                            filters.put("brand", brand);
                            filters.put("priceRange", priceRange);
                            filters.put("sortBy", sortBy);
                            data.put("filters", filters);

                        response.put("success", true);
                        response.put("data", data);
                        return ResponseEntity.ok(response);
                } catch (Exception e) {
                        response.put("success", false);
                        response.put("message", "Lỗi khi tải danh sách sản phẩm: " + e.getMessage());
                        return ResponseEntity.badRequest().body(response);
                }
    }

    @Operation(summary = "Get newest products")
    @GetMapping("/newest")
    public ResponseEntity<Map<String, Object>> newest(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
                Map<String, Object> response = new HashMap<>();
                try {
                        Pageable pageable = PageRequest.of(page, size);
                        Page<Watch> watchPage = watchService.findNewestProducts(pageable);

                        Map<String, Object> data = new HashMap<>();
                        data.put("items", ApiDataMapper.mapWatches(watchPage.getContent()));
                        data.put("page", ApiDataMapper.pageInfo(watchPage));
                        data.put("pageTitle", "Sản phẩm mới nhất");

                        response.put("success", true);
                        response.put("data", data);
                        return ResponseEntity.ok(response);
                } catch (Exception e) {
                        response.put("success", false);
                        response.put("message", "Lỗi khi tải sản phẩm mới nhất: " + e.getMessage());
                        return ResponseEntity.badRequest().body(response);
                }
    }

    @Operation(summary = "Get discount products")
    @GetMapping("/discount")
    public ResponseEntity<Map<String, Object>> discount(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
                Map<String, Object> response = new HashMap<>();
                try {
                        Pageable pageable = PageRequest.of(page, size);
                        Page<Watch> watchPage = watchService.findDiscountProducts(pageable);

                        Map<String, Object> data = new HashMap<>();
                        data.put("items", ApiDataMapper.mapWatches(watchPage.getContent()));
                        data.put("page", ApiDataMapper.pageInfo(watchPage));
                        data.put("pageTitle", "Sản phẩm giảm giá");

                        response.put("success", true);
                        response.put("data", data);
                        return ResponseEntity.ok(response);
                } catch (Exception e) {
                        response.put("success", false);
                        response.put("message", "Lỗi khi tải sản phẩm giảm giá: " + e.getMessage());
                        return ResponseEntity.badRequest().body(response);
                }
    }

    @Operation(summary = "Get product detail")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> detail(@PathVariable Integer id) {
                Map<String, Object> response = new HashMap<>();
                try {
                        Watch watch = watchService.findById(id)
                                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

                        List<Watch> relatedWatches = watchService.findRelatedProducts(watch, 4);

                        Map<String, Object> data = new HashMap<>();
                        data.put("watch", ApiDataMapper.watchDetail(watch));
                        data.put("relatedWatches", ApiDataMapper.mapWatches(relatedWatches));

                        response.put("success", true);
                        response.put("data", data);
                        return ResponseEntity.ok(response);
                } catch (Exception e) {
                        response.put("success", false);
                        response.put("message", e.getMessage());
                        return ResponseEntity.badRequest().body(response);
                }
        }

        private String getPriceRangeLabel(String priceRange) {
                switch (priceRange) {
                        case "under-1m":
                                return "Dưới 1 triệu";
                        case "1m-3m":
                                return "1 - 3 triệu";
                        case "3m-5m":
                                return "3 - 5 triệu";
                        case "5m-10m":
                                return "5 - 10 triệu";
                        case "over-10m":
                                return "Trên 10 triệu";
                        default:
                                return priceRange;
                }
    }
}
