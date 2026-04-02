package boiz.shop._2BShop.controller.api.admin;

import boiz.shop._2BShop.controller.api.ApiDataMapper;
import boiz.shop._2BShop.controller.api.ApiValueParser;
import boiz.shop._2BShop.entity.WatchBrand;
import boiz.shop._2BShop.respository.WatchBrandRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Admin Brand API")
@RestController
@RequestMapping("/api/v1/admin/brands")
public class BrandAdminApiController {

    @Autowired
    private WatchBrandRepository brandRepository;

    @Operation(summary = "Get brands")
    @GetMapping
    public ResponseEntity<Map<String, Object>> list() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<WatchBrand> brands = brandRepository.findAll();
            long activeBrandCount = brands.stream().filter(b -> Boolean.TRUE.equals(b.getIsActive())).count();
            long inactiveBrandCount = brands.stream().filter(b -> !Boolean.TRUE.equals(b.getIsActive())).count();

            Map<String, Object> data = new HashMap<>();
            data.put("items", ApiDataMapper.mapBrands(brands));
            data.put("activeBrandCount", activeBrandCount);
            data.put("inactiveBrandCount", inactiveBrandCount);

            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Get brand detail")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> detail(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            WatchBrand brand = brandRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy brand!"));

            response.put("success", true);
            response.put("data", ApiDataMapper.brand(brand));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Create brand")
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody Map<String, Object> payload) {
        return saveBrandInternal(null, payload);
    }

    @Operation(summary = "Update brand")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        return saveBrandInternal(id, payload);
    }

    @Operation(summary = "Delete brand")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            WatchBrand brand = brandRepository.findById(id).orElse(null);
            if (brand == null) {
                throw new RuntimeException("Không tìm thấy brand!");
            }

            if (brand.getWatches() != null && !brand.getWatches().isEmpty()) {
                brand.setIsActive(false);
                brandRepository.save(brand);

                response.put("success", true);
                response.put("message", "Brand đã được ẩn (vẫn có " + brand.getWatches().size() + " sản phẩm liên kết)");
                response.put("action", "inactivated");
                return ResponseEntity.ok(response);
            }

            brandRepository.delete(brand);
            response.put("success", true);
            response.put("message", "Xóa brand thành công!");
            response.put("action", "deleted");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi khi xóa: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Activate brand")
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Map<String, Object>> activate(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        try {
            WatchBrand brand = brandRepository.findById(id).orElse(null);
            if (brand == null) {
                throw new RuntimeException("Không tìm thấy brand!");
            }

            brand.setIsActive(true);
            brandRepository.save(brand);

            response.put("success", true);
            response.put("message", "Kích hoạt brand thành công!");
            response.put("data", ApiDataMapper.brand(brand));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private ResponseEntity<Map<String, Object>> saveBrandInternal(Integer id, Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            WatchBrand brand = id == null
                    ? new WatchBrand()
                    : brandRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy brand!"));

            String brandName = ApiValueParser.asString(payload.get("brandName"));
            if (brandName != null) {
                WatchBrand existing = brandRepository.findAll().stream()
                        .filter(b -> b.getBrandName().equalsIgnoreCase(brandName)
                                && (brand.getBrandId() == null || !b.getBrandId().equals(brand.getBrandId())))
                        .findFirst()
                        .orElse(null);
                if (existing != null) {
                    throw new RuntimeException("Tên brand đã tồn tại!");
                }
                brand.setBrandName(brandName);
            }

            if (payload.containsKey("description")) {
                brand.setDescription(ApiValueParser.asString(payload.get("description")));
            }
            if (payload.containsKey("logoUrl")) {
                brand.setLogoUrl(ApiValueParser.asString(payload.get("logoUrl")));
            }
            if (payload.containsKey("isActive")) {
                brand.setIsActive(ApiValueParser.asBoolean(payload.get("isActive")));
            }

            if (brand.getIsActive() == null) {
                brand.setIsActive(true);
            }

            WatchBrand saved = brandRepository.save(brand);

            response.put("success", true);
            response.put("message", id == null ? "Thêm brand thành công!" : "Cập nhật brand thành công!");
            response.put("data", ApiDataMapper.brand(saved));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Lỗi: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
