package boiz.shop._2BShop.api.controller.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import boiz.shop._2BShop.api.mapper.ApiMapper;
import boiz.shop._2BShop.api.response.ApiResponse;
import boiz.shop._2BShop.api.response.PaginatedResponse;
import boiz.shop._2BShop.entity.Watch;
import boiz.shop._2BShop.entity.WatchImage;
import boiz.shop._2BShop.respository.OrderDetailRepository;
import boiz.shop._2BShop.respository.WatchBrandRepository;
import boiz.shop._2BShop.respository.WatchCategoryRepository;
import boiz.shop._2BShop.respository.WatchImageRepository;
import boiz.shop._2BShop.respository.WatchRepository;
import boiz.shop._2BShop.service.FileUploadService;

@RestController
@RequestMapping("/api/v1/admin/watches")
public class AdminWatchApiController {

    private final WatchRepository watchRepository;
    private final WatchBrandRepository watchBrandRepository;
    private final WatchCategoryRepository watchCategoryRepository;
    private final WatchImageRepository watchImageRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final FileUploadService fileUploadService;
    private final ApiMapper apiMapper;

    public AdminWatchApiController(
            WatchRepository watchRepository,
            WatchBrandRepository watchBrandRepository,
            WatchCategoryRepository watchCategoryRepository,
            WatchImageRepository watchImageRepository,
            OrderDetailRepository orderDetailRepository,
            FileUploadService fileUploadService,
            ApiMapper apiMapper) {
        this.watchRepository = watchRepository;
        this.watchBrandRepository = watchBrandRepository;
        this.watchCategoryRepository = watchCategoryRepository;
        this.watchImageRepository = watchImageRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.fileUploadService = fileUploadService;
        this.apiMapper = apiMapper;
    }

    @GetMapping
    public PaginatedResponse<?> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("createdDate").descending());
        Page<Watch> watches;
        if (keyword != null && !keyword.isBlank()) {
            watches = watchRepository.findByWatchNameContainingIgnoreCase(keyword, pageable);
        } else if (brandId != null) {
            watches = watchRepository.findByBrandBrandId(brandId, pageable);
        } else if (categoryId != null) {
            watches = watchRepository.findByCategoryCategoryId(categoryId, pageable);
        } else if (isActive != null) {
            watches = watchRepository.findByIsActive(isActive, pageable);
        } else {
            watches = watchRepository.findAll(pageable);
        }
        return PaginatedResponse.from(watches, apiMapper::toProductCardDto);
    }

    @GetMapping("/options")
    public ApiResponse<Map<String, ?>> options() {
        return ApiResponse.success(Map.of(
                "brands", watchBrandRepository.findAll().stream().map(apiMapper::toBrandDto).toList(),
                "categories", watchCategoryRepository.findAll().stream().map(apiMapper::toCategoryDto).toList()));
    }

    @GetMapping("/{id}")
    public ApiResponse<?> detail(@PathVariable Integer id) {
        return ApiResponse.success(apiMapper.toProductDetailDto(findWatch(id), List.of()));
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ApiResponse<?> create(
            @RequestParam String watchName,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal price,
            @RequestParam(defaultValue = "0") Integer discountPercent,
            @RequestParam(defaultValue = "0") Integer stockQuantity,
            @RequestParam(defaultValue = "true") Boolean isActive,
            @RequestParam Integer brandId,
            @RequestParam Integer categoryId,
            @RequestParam(required = false) MultipartFile mainImage,
            @RequestParam(required = false) List<MultipartFile> galleryImages) throws Exception {
        Watch watch = new Watch();
        applyWatchFields(watch, watchName, description, price, discountPercent, stockQuantity, isActive, brandId, categoryId);
        watch.setCreatedDate(LocalDateTime.now());
        watch.setUpdatedDate(LocalDateTime.now());
        watch.setSoldCount(0);
        Watch saved = watchRepository.save(watch);
        handleImages(saved, mainImage, galleryImages);
        return ApiResponse.success("Tạo sản phẩm thành công", apiMapper.toProductDetailDto(findWatch(saved.getWatchId()), List.of()));
    }

    @PostMapping(path = "/{id}", consumes = {"multipart/form-data"})
    public ApiResponse<?> update(
            @PathVariable Integer id,
            @RequestParam String watchName,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal price,
            @RequestParam(defaultValue = "0") Integer discountPercent,
            @RequestParam(defaultValue = "0") Integer stockQuantity,
            @RequestParam(defaultValue = "true") Boolean isActive,
            @RequestParam Integer brandId,
            @RequestParam Integer categoryId,
            @RequestParam(required = false) MultipartFile mainImage,
            @RequestParam(required = false) List<MultipartFile> galleryImages) throws Exception {
        Watch watch = findWatch(id);
        applyWatchFields(watch, watchName, description, price, discountPercent, stockQuantity, isActive, brandId, categoryId);
        watch.setUpdatedDate(LocalDateTime.now());
        Watch saved = watchRepository.save(watch);
        handleImages(saved, mainImage, galleryImages);
        return ApiResponse.success("Cập nhật sản phẩm thành công", apiMapper.toProductDetailDto(findWatch(saved.getWatchId()), List.of()));
    }

    @PostMapping("/{id}/toggle-active")
    public ApiResponse<?> toggleActive(@PathVariable Integer id) {
        Watch watch = findWatch(id);
        watch.setIsActive(!Boolean.TRUE.equals(watch.getIsActive()));
        watch.setUpdatedDate(LocalDateTime.now());
        return ApiResponse.success("Đã cập nhật trạng thái sản phẩm", apiMapper.toProductCardDto(watchRepository.save(watch)));
    }

    @PostMapping("/{id}/stock")
    public ApiResponse<?> updateStock(@PathVariable Integer id, @RequestParam Integer stockQuantity) {
        Watch watch = findWatch(id);
        watch.setStockQuantity(stockQuantity);
        watch.setUpdatedDate(LocalDateTime.now());
        return ApiResponse.success("Cập nhật tồn kho thành công", apiMapper.toProductCardDto(watchRepository.save(watch)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Integer id) {
        Watch watch = findWatch(id);
        boolean hasOrders = orderDetailRepository.existsByWatchWatchId(id);
        if (hasOrders) {
            watch.setIsActive(false);
            watch.setUpdatedDate(LocalDateTime.now());
            return ApiResponse.success(
                    "Sản phẩm đã có trong đơn hàng nên được chuyển sang inactive",
                    apiMapper.toProductCardDto(watchRepository.save(watch)));
        }
        if (watch.getImages() != null) {
            for (WatchImage image : watch.getImages()) {
                try {
                    fileUploadService.deleteWatchImage(image.getImageUrl());
                } catch (Exception ignored) {
                }
            }
        }
        watchRepository.delete(watch);
        return ApiResponse.success("Xóa sản phẩm thành công", null);
    }

    private Watch findWatch(Integer id) {
        return watchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
    }

    private void applyWatchFields(
            Watch watch,
            String watchName,
            String description,
            BigDecimal price,
            Integer discountPercent,
            Integer stockQuantity,
            Boolean isActive,
            Integer brandId,
            Integer categoryId) {
        watch.setWatchName(watchName);
        watch.setDescription(description);
        watch.setPrice(price);
        watch.setDiscountPercent(discountPercent);
        watch.setStockQuantity(stockQuantity);
        watch.setIsActive(isActive);
        watch.setBrand(watchBrandRepository.findById(brandId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu")));
        watch.setCategory(watchCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục")));
    }

    private void handleImages(Watch watch, MultipartFile mainImage, List<MultipartFile> galleryImages) throws Exception {
        if (mainImage != null && !mainImage.isEmpty()) {
            String imagePath = fileUploadService.uploadWatchMainImage(mainImage, watch.getWatchId());
            WatchImage main = watchImageRepository.findByWatchWatchIdAndIsPrimaryTrue(watch.getWatchId())
                    .orElse(new WatchImage());
            main.setWatch(watch);
            main.setImageUrl(imagePath);
            main.setIsPrimary(true);
            watchImageRepository.save(main);
        }
        if (galleryImages != null) {
            for (MultipartFile file : galleryImages) {
                if (file != null && !file.isEmpty()) {
                    String imagePath = fileUploadService.uploadWatchGalleryImage(file, watch.getWatchId());
                    watchImageRepository.save(new WatchImage(null, imagePath, false, watch));
                }
            }
        }
    }
}
