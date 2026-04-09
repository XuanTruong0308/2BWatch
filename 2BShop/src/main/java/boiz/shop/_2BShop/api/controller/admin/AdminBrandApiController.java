package boiz.shop._2BShop.api.controller.admin;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import boiz.shop._2BShop.api.dto.BrandDto;
import boiz.shop._2BShop.api.mapper.ApiMapper;
import boiz.shop._2BShop.api.request.BrandUpsertRequest;
import boiz.shop._2BShop.api.response.ApiResponse;
import boiz.shop._2BShop.entity.WatchBrand;
import boiz.shop._2BShop.respository.WatchBrandRepository;

@RestController
@RequestMapping("/api/v1/admin/brands")
public class AdminBrandApiController {

    private final WatchBrandRepository brandRepository;
    private final ApiMapper apiMapper;

    public AdminBrandApiController(WatchBrandRepository brandRepository, ApiMapper apiMapper) {
        this.brandRepository = brandRepository;
        this.apiMapper = apiMapper;
    }

    @GetMapping
    public ApiResponse<List<BrandDto>> list() {
        return ApiResponse.success(brandRepository.findAll().stream().map(apiMapper::toBrandDto).toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<BrandDto> detail(@PathVariable Integer id) {
        return ApiResponse.success(apiMapper.toBrandDto(findBrand(id)));
    }

    @PostMapping
    public ApiResponse<BrandDto> create(@RequestBody BrandUpsertRequest request) {
        validateDuplicate(null, request.brandName());
        WatchBrand brand = new WatchBrand();
        brand.setBrandName(request.brandName());
        brand.setDescription(request.description());
        brand.setLogoUrl(request.logoUrl());
        brand.setIsActive(request.active() == null ? true : request.active());
        return ApiResponse.success("Thêm brand thành công", apiMapper.toBrandDto(brandRepository.save(brand)));
    }

    @PutMapping("/{id}")
    public ApiResponse<BrandDto> update(@PathVariable Integer id, @RequestBody BrandUpsertRequest request) {
        WatchBrand brand = findBrand(id);
        validateDuplicate(id, request.brandName());
        brand.setBrandName(request.brandName());
        brand.setDescription(request.description());
        brand.setLogoUrl(request.logoUrl());
        brand.setIsActive(request.active() == null ? brand.getIsActive() : request.active());
        return ApiResponse.success("Cập nhật brand thành công", apiMapper.toBrandDto(brandRepository.save(brand)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<BrandDto> delete(@PathVariable Integer id) {
        WatchBrand brand = findBrand(id);
        if (brand.getWatches() != null && !brand.getWatches().isEmpty()) {
            brand.setIsActive(false);
            return ApiResponse.success(
                    "Brand đã được ẩn vì vẫn còn sản phẩm liên kết",
                    apiMapper.toBrandDto(brandRepository.save(brand)));
        }
        brandRepository.delete(brand);
        return ApiResponse.success("Xóa brand thành công", null);
    }

    @PostMapping("/{id}/activate")
    public ApiResponse<BrandDto> activate(@PathVariable Integer id) {
        WatchBrand brand = findBrand(id);
        brand.setIsActive(true);
        return ApiResponse.success("Kích hoạt brand thành công", apiMapper.toBrandDto(brandRepository.save(brand)));
    }

    private WatchBrand findBrand(Integer id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy brand"));
    }

    private void validateDuplicate(Integer brandId, String brandName) {
        boolean duplicated = brandRepository.findAll().stream()
                .anyMatch(brand -> brand.getBrandName().equalsIgnoreCase(brandName)
                        && (brandId == null || !brand.getBrandId().equals(brandId)));
        if (duplicated) {
            throw new RuntimeException("Tên brand đã tồn tại");
        }
    }
}
