package boiz.shop._2BShop.controller.admin;

import boiz.shop._2BShop.entity.WatchBrand;
import boiz.shop._2BShop.respository.WatchBrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/brands")
public class BrandAdminController {

    @Autowired
    private WatchBrandRepository brandRepository;

    /**
     * Hiển thị danh sách brands
     */
    @GetMapping
    public String listBrands(Model model) {
        List<WatchBrand> brands = brandRepository.findAll();
        long activeBrandCount = brands.stream().filter(b -> b.getIsActive() != null && b.getIsActive()).count();
        long inactiveBrandCount = brands.stream().filter(b -> b.getIsActive() == null || !b.getIsActive()).count();
        
        model.addAttribute("brands", brands);
        model.addAttribute("activeBrandCount", activeBrandCount);
        model.addAttribute("inactiveBrandCount", inactiveBrandCount);
        return "admin/brands";
    }

    /**
     * Form thêm brand mới
     */
    @GetMapping("/new")
    public String newBrandForm(Model model) {
        model.addAttribute("brand", new WatchBrand());
        model.addAttribute("isEdit", false);
        return "admin/brand-form";
    }

    /**
     * Form sửa brand
     */
    @GetMapping("/{id}/edit")
    public String editBrandForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        WatchBrand brand = brandRepository.findById(id).orElse(null);
        
        if (brand == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy brand!");
            return "redirect:/admin/brands";
        }
        
        model.addAttribute("brand", brand);
        model.addAttribute("isEdit", true);
        return "admin/brand-form";
    }

    /**
     * Lưu brand (thêm mới hoặc cập nhật)
     */
    @PostMapping("/save")
    public String saveBrand(@ModelAttribute WatchBrand brand, RedirectAttributes redirectAttributes) {
        try {
            // Kiểm tra trùng tên brand (ngoại trừ chính nó khi edit)
            WatchBrand existing = brandRepository.findAll().stream()
                    .filter(b -> b.getBrandName().equalsIgnoreCase(brand.getBrandName()) 
                              && !b.getBrandId().equals(brand.getBrandId()))
                    .findFirst()
                    .orElse(null);
            
            if (existing != null) {
                redirectAttributes.addFlashAttribute("error", "Tên brand đã tồn tại!");
                return "redirect:/admin/brands/new";
            }
            
            // Set default values
            if (brand.getIsActive() == null) {
                brand.setIsActive(true);
            }
            
            brandRepository.save(brand);
            
            String message = brand.getBrandId() == null ? "Thêm brand thành công!" : "Cập nhật brand thành công!";
            redirectAttributes.addFlashAttribute("success", message);
            return "redirect:/admin/brands";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/brands";
        }
    }

    /**
     * Xóa brand (soft delete - set isActive = false)
     */
    @PostMapping("/{id}/delete")
    public String deleteBrand(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            WatchBrand brand = brandRepository.findById(id).orElse(null);
            
            if (brand == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy brand!");
                return "redirect:/admin/brands";
            }
            
            // Kiểm tra xem brand có sản phẩm nào không
            if (brand.getWatches() != null && !brand.getWatches().isEmpty()) {
                // Soft delete
                brand.setIsActive(false);
                brandRepository.save(brand);
                redirectAttributes.addFlashAttribute("warning", "Brand đã được ẩn (vẫn có " + brand.getWatches().size() + " sản phẩm liên kết)");
            } else {
                // Hard delete nếu không có sản phẩm
                brandRepository.delete(brand);
                redirectAttributes.addFlashAttribute("success", "Xóa brand thành công!");
            }
            
            return "redirect:/admin/brands";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa: " + e.getMessage());
            return "redirect:/admin/brands";
        }
    }

    /**
     * Kích hoạt lại brand
     */
    @PostMapping("/{id}/activate")
    public String activateBrand(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            WatchBrand brand = brandRepository.findById(id).orElse(null);
            
            if (brand == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy brand!");
                return "redirect:/admin/brands";
            }
            
            brand.setIsActive(true);
            brandRepository.save(brand);
            
            redirectAttributes.addFlashAttribute("success", "Kích hoạt brand thành công!");
            return "redirect:/admin/brands";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/brands";
        }
    }
}
