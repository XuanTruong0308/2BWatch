package boiz.shop._2BShop.api.controller.publics;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import boiz.shop._2BShop.api.dto.HomePageDto;
import boiz.shop._2BShop.api.dto.OptionDto;
import boiz.shop._2BShop.api.dto.ProductDetailDto;
import boiz.shop._2BShop.api.mapper.ApiMapper;
import boiz.shop._2BShop.api.request.ContactRequest;
import boiz.shop._2BShop.api.response.ApiResponse;
import boiz.shop._2BShop.api.response.PaginatedResponse;
import boiz.shop._2BShop.entity.Watch;
import boiz.shop._2BShop.respository.WatchBrandRepository;
import boiz.shop._2BShop.respository.WatchCategoryRepository;
import boiz.shop._2BShop.service.MailService;
import boiz.shop._2BShop.service.WatchService;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1/public")
public class PublicCatalogApiController {

    private final WatchService watchService;
    private final WatchBrandRepository watchBrandRepository;
    private final WatchCategoryRepository watchCategoryRepository;
    private final ApiMapper apiMapper;
    private final MailService mailService;

    public PublicCatalogApiController(
            WatchService watchService,
            WatchBrandRepository watchBrandRepository,
            WatchCategoryRepository watchCategoryRepository,
            ApiMapper apiMapper,
            MailService mailService) {
        this.watchService = watchService;
        this.watchBrandRepository = watchBrandRepository;
        this.watchCategoryRepository = watchCategoryRepository;
        this.apiMapper = apiMapper;
        this.mailService = mailService;
    }

    @GetMapping("/home")
    public ApiResponse<HomePageDto> home() {
        List<OptionDto> brands = watchBrandRepository.findByIsActiveTrueOrderByBrandName().stream()
                .map(apiMapper::toOptionDto)
                .toList();
        List<OptionDto> categories = watchCategoryRepository.findByIsActiveTrueOrderByCategoryName().stream()
                .map(apiMapper::toOptionDto)
                .toList();

        return ApiResponse.success(new HomePageDto(
                watchService.getTop3BestSellers().stream().map(apiMapper::toProductCardDto).toList(),
                watchService.getTop3Newest().stream().map(apiMapper::toProductCardDto).toList(),
                watchService.getTop3BiggestDiscount().stream().map(apiMapper::toProductCardDto).toList(),
                brands,
                categories));
    }

    @GetMapping("/options")
    public ApiResponse<?> options() {
        return ApiResponse.success(new HomePageDto(
                List.of(),
                List.of(),
                List.of(),
                watchBrandRepository.findByIsActiveTrueOrderByBrandName().stream().map(apiMapper::toOptionDto).toList(),
                watchCategoryRepository.findByIsActiveTrueOrderByCategoryName().stream().map(apiMapper::toOptionDto).toList()));
    }

    @GetMapping("/watches")
    public PaginatedResponse<?> listProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        Pageable pageable = createPageable(page, size, sortBy);
        Page<Watch> watchPage;

        if (search != null && !search.isBlank()) {
            watchPage = watchService.search(search, pageable);
        } else if (category != null && !category.isBlank()) {
            watchPage = watchService.findByCategory(category, pageable);
        } else if (brand != null && !brand.isBlank()) {
            watchPage = watchService.findByBrand(brand, pageable);
        } else if (priceRange != null && !priceRange.isBlank()) {
            watchPage = watchService.findByPriceRange(priceRange, pageable);
        } else {
            watchPage = watchService.findActiveProducts(pageable);
        }

        return PaginatedResponse.from(watchPage, apiMapper::toProductCardDto);
    }

    @GetMapping("/watches/newest")
    public PaginatedResponse<?> newestProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        return PaginatedResponse.from(
                watchService.findNewestProducts(PageRequest.of(
                        page,
                        size,
                        Sort.by(Sort.Order.desc("createdDate"), Sort.Order.desc("watchId")))),
                apiMapper::toProductCardDto);
    }

    @GetMapping("/watches/discount")
    public PaginatedResponse<?> discountProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        return PaginatedResponse.from(
                watchService.findDiscountProducts(PageRequest.of(
                        page,
                        size,
                        Sort.by(Sort.Order.desc("discountPercent"), Sort.Order.desc("createdDate"), Sort.Order.desc("watchId")))),
                apiMapper::toProductCardDto);
    }

    @GetMapping("/watches/{id}")
    public ApiResponse<ProductDetailDto> productDetail(@PathVariable Integer id) {
        Watch watch = watchService.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
        return ApiResponse.success(apiMapper.toProductDetailDto(watch, watchService.findRelatedProducts(watch, 4)));
    }

    @PostMapping("/contact")
    public ApiResponse<Void> contact(@RequestBody ContactRequest request) {
        mailService.sendContactEmail(request.name(), request.email(), request.subject(), request.message());
        return ApiResponse.success("Gửi thông tin liên hệ thành công. Chúng tôi sẽ phản hồi sớm nhất.", null);
    }

    private Pageable createPageable(int page, int size, String sortBy) {
        if ("price-asc".equals(sortBy)) {
            return PageRequest.of(page, size, Sort.by("price").ascending());
        }
        if ("price-desc".equals(sortBy)) {
            return PageRequest.of(page, size, Sort.by("price").descending());
        }
        return PageRequest.of(page, size, Sort.by("createdDate").descending());
    }
}
