package boiz.shop._2BShop.controller.api.admin;

import boiz.shop._2BShop.controller.api.ApiDataMapper;
import boiz.shop._2BShop.controller.api.ApiValueParser;
import boiz.shop._2BShop.entity.Watch;
import boiz.shop._2BShop.entity.WatchBrand;
import boiz.shop._2BShop.entity.WatchCategory;
import boiz.shop._2BShop.entity.WatchImage;
import boiz.shop._2BShop.respository.OrderDetailRepository;
import boiz.shop._2BShop.respository.WatchBrandRepository;
import boiz.shop._2BShop.respository.WatchCategoryRepository;
import boiz.shop._2BShop.respository.WatchImageRepository;
import boiz.shop._2BShop.respository.WatchRepository;
import boiz.shop._2BShop.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Admin Watch API")
@RestController
@RequestMapping("/api/v1/admin/watches")
public class WatchAdminApiController {

    @Autowired
    private WatchRepository watchRepo;

    @Autowired
    private WatchBrandRepository watchBrandRepo;

    @Autowired
    private WatchCategoryRepository watchCategoryRepo;

    @Autowired
    private WatchImageRepository watchImageRepo;

    @Autowired
    private OrderDetailRepository orderDetailRepo;

    @Autowired
    private FileUploadService fileUploadService;

    @Operation(summary = "Get watches for admin")
    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Boolean isActive) {
        Map<String, Object> response = new HashMap<>();
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
            Page<Watch> watches;

            if (keyword != null && !keyword.trim().isEmpty()) {
                watches = watchRepo.findByWatchNameContainingIgnoreCase(keyword, pageable);
            } else if (brandId != null) {
                watches = watchRepo.findByBrandBrandId(brandId, pageable);
            } else if (categoryId != null) {
                watches = watchRepo.findByCategoryCategoryId(categoryId, pageable);
            } else if (isActive != null) {
                watches = watchRepo.findByIsActive(isActive, pageable);
            } else {
                watches = watchRepo.findAll(pageable);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("items", ApiDataMapper.mapWatches(watches.getContent()));
            data.put("page", ApiDataMapper.pageInfo(watches));
            data.put("brands", ApiDataMapper.mapBrands(watchBrandRepo.findAll()));
            data.put("categories", ApiDataMapper.mapCategories(watchCategoryRepo.findAll()));

            Map<String, Object> filters = new HashMap<>();
            filters.put("keyword", keyword);
            filters.put("brandId", brandId);
            filters.put("categoryId", categoryId);
            filters.put("isActive", isActive);
            data.put("filters", filters);

            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi tải danh sách sản phẩm: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Get watch detail")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> detail(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Watch watch = watchRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đồng hồ với id: " + id));

            Map<String, Object> data = new HashMap<>();
            data.put("watch", ApiDataMapper.watchDetail(watch));
            data.put("brands", ApiDataMapper.mapBrands(watchBrandRepo.findAll()));
            data.put("categories", ApiDataMapper.mapCategories(watchCategoryRepo.findAll()));

            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Create watch")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> create(
            @ModelAttribute Watch watch,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) MultipartFile mainImage,
            @RequestParam(required = false) List<MultipartFile> galleryImages) {
        return saveWatchInternal(watch, brandId, categoryId, mainImage, galleryImages);
    }

    @Operation(summary = "Update watch")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> update(
            @PathVariable Integer id,
            @ModelAttribute Watch watch,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) MultipartFile mainImage,
            @RequestParam(required = false) List<MultipartFile> galleryImages) {
        watch.setWatchId(id);
        return saveWatchInternal(watch, brandId, categoryId, mainImage, galleryImages);
    }

    @Operation(summary = "Delete watch")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Watch watch = watchRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đồng hồ"));

            boolean hasOrders = orderDetailRepo.existsByWatchWatchId(id);
            if (hasOrders) {
                watch.setIsActive(false);
                watch.setUpdatedDate(LocalDateTime.now());
                watchRepo.save(watch);

                response.put("success", true);
                response.put("message", "Sản phẩm đã có trong đơn hàng. Đã chuyển sang trạng thái Inactive.");
                response.put("action", "inactivated");
                return ResponseEntity.ok(response);
            }

            List<WatchImage> images = watch.getImages();
            if (images != null) {
                for (WatchImage image : images) {
                    try {
                        fileUploadService.deleteWatchImage(image.getImageUrl());
                    } catch (Exception ignored) {
                    }
                }
            }

            watchRepo.deleteById(id);
            response.put("success", true);
            response.put("message", "Xóa sản phẩm thành công!");
            response.put("action", "deleted");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Toggle watch active status")
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<Map<String, Object>> toggleActive(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Watch watch = watchRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đồng hồ"));

            watch.setIsActive(!watch.getIsActive());
            watch.setUpdatedDate(LocalDateTime.now());
            watchRepo.save(watch);

            response.put("success", true);
            response.put("message", "Đã chuyển sản phẩm sang " + (watch.getIsActive() ? "Active" : "Inactive"));
            response.put("isActive", watch.getIsActive());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Update watch stock")
    @PatchMapping("/{id}/stock")
    public ResponseEntity<Map<String, Object>> updateStock(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            Integer stockQuantity = ApiValueParser.asInteger(payload.get("stockQuantity"));
            if (stockQuantity == null) {
                throw new RuntimeException("Thiếu stockQuantity");
            }

            Watch watch = watchRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đồng hồ"));

            watch.setStockQuantity(stockQuantity);
            watch.setUpdatedDate(LocalDateTime.now());
            watchRepo.save(watch);

            response.put("success", true);
            response.put("message", "Cập nhật tồn kho thành công!");
            response.put("stockQuantity", watch.getStockQuantity());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private ResponseEntity<Map<String, Object>> saveWatchInternal(
            Watch watch,
            Integer brandId,
            Integer categoryId,
            MultipartFile mainImage,
            List<MultipartFile> galleryImages) {
        Map<String, Object> response = new HashMap<>();
        try {
            bindBrandAndCategory(watch, brandId, categoryId);

            if (watch.getWatchId() == null) {
                watch.setCreatedDate(LocalDateTime.now());
                if (watch.getSoldCount() == null) {
                    watch.setSoldCount(0);
                }
            }
            watch.setUpdatedDate(LocalDateTime.now());

            Watch savedWatch = watchRepo.save(watch);

            if (mainImage != null && !mainImage.isEmpty()) {
                String imagePath = fileUploadService.uploadWatchMainImage(mainImage, savedWatch.getWatchId());

                WatchImage mainImg = null;
                if (savedWatch.getImages() != null) {
                    for (WatchImage image : savedWatch.getImages()) {
                        if (Boolean.TRUE.equals(image.getIsPrimary())) {
                            mainImg = image;
                            break;
                        }
                    }
                }

                if (mainImg == null) {
                    mainImg = new WatchImage();
                }

                mainImg.setWatch(savedWatch);
                mainImg.setImageUrl(imagePath);
                mainImg.setIsPrimary(true);
                watchImageRepo.save(mainImg);
            }

            if (galleryImages != null && !galleryImages.isEmpty()) {
                for (MultipartFile file : galleryImages) {
                    if (file != null && !file.isEmpty()) {
                        String imagePath = fileUploadService.uploadWatchGalleryImage(file, savedWatch.getWatchId());
                        WatchImage galleryImg = new WatchImage();
                        galleryImg.setWatch(savedWatch);
                        galleryImg.setImageUrl(imagePath);
                        galleryImg.setIsPrimary(false);
                        watchImageRepo.save(galleryImg);
                    }
                }
            }

            response.put("success", true);
            response.put("message", "Lưu sản phẩm thành công!");
            response.put("data", ApiDataMapper.watchDetail(watchRepo.findById(savedWatch.getWatchId()).orElse(savedWatch)));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private void bindBrandAndCategory(Watch watch, Integer brandId, Integer categoryId) {
        Integer resolvedBrandId = brandId;
        if (resolvedBrandId == null && watch.getBrand() != null) {
            resolvedBrandId = watch.getBrand().getBrandId();
        }

        Integer resolvedCategoryId = categoryId;
        if (resolvedCategoryId == null && watch.getCategory() != null) {
            resolvedCategoryId = watch.getCategory().getCategoryId();
        }

        final Integer brandIdToUse = resolvedBrandId;
        final Integer categoryIdToUse = resolvedCategoryId;

        if (brandIdToUse != null) {
            WatchBrand brand = watchBrandRepo.findById(brandIdToUse)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy brand với id: " + brandIdToUse));
            watch.setBrand(brand);
        }

        if (categoryIdToUse != null) {
            WatchCategory category = watchCategoryRepo.findById(categoryIdToUse)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy category với id: " + categoryIdToUse));
            watch.setCategory(category);
        }
    }
}
