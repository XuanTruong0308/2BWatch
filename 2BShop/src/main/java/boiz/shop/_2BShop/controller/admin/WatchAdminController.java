package boiz.shop._2BShop.controller.admin;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import boiz.shop._2BShop.entity.Watch;
import boiz.shop._2BShop.entity.WatchImage;
import boiz.shop._2BShop.respository.OrderDetailRepository;
import boiz.shop._2BShop.respository.WatchBrandRepository;
import boiz.shop._2BShop.respository.WatchCategoryRepository;
import boiz.shop._2BShop.respository.WatchImageRepository;
import boiz.shop._2BShop.respository.WatchRepository;
import boiz.shop._2BShop.service.FileUploadService;

/**
 * Watch Admin Controller
 * Handles watch CRUD operations for admin
 * URL: /admin/watches
 */
@Controller
@RequestMapping("/admin/watches")
public class WatchAdminController {

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

    /**
     * List all watches với pagination & filters
     * URL: /admin/watches
     */
    @GetMapping
    public String listWatches(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            Model model) {
        Pageable pageable = PageRequest.of(page, 20, Sort.by("createdDate").descending());

        Page<Watch> watches;

        // Apply filters
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

        model.addAttribute("watches", watches);
        model.addAttribute("brands", watchBrandRepo.findAll());
        model.addAttribute("categories", watchCategoryRepo.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedBrandId", brandId);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedIsActive", isActive);

        return "admin/watches";
    }

    /**
     * Show form to add new watch
     * URL: /admin/watches/new
     */
    @GetMapping("/new")
    public String newWatchForm(Model model) {
        Watch watch = new Watch();
        watch.setIsActive(true);
        watch.setDiscountPercent(0);
        watch.setStockQuantity(0);
        watch.setSoldCount(0);

        model.addAttribute("watch", watch);
        model.addAttribute("brands", watchBrandRepo.findAll());
        model.addAttribute("categories", watchCategoryRepo.findAll());
        model.addAttribute("isEdit", false);

        return "admin/watch-form";
    }

    /**
     * Show form to edit watch
     * URL: /admin/watches/edit/{id}
     */
    @GetMapping("/edit/{id}")
    public String editWatchForm(@PathVariable Integer id, Model model) {
        Watch watch = watchRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đồng hồ với id: " + id));

        model.addAttribute("watch", watch);
        model.addAttribute("brands", watchBrandRepo.findAll());
        model.addAttribute("categories", watchCategoryRepo.findAll());
        model.addAttribute("isEdit", true);

        return "admin/watch-form";
    }

    /**
     * Save watch (create or update)
     * URL: POST /admin/watches/save
     */
    @PostMapping("/save")
    public String saveWatch(
            @ModelAttribute Watch watch,
            @RequestParam(required = false) MultipartFile mainImage,
            @RequestParam(required = false) List<MultipartFile> galleryImages,
            RedirectAttributes redirectAttributes) {
        try {
            // Set timestamps
            if (watch.getWatchId() == null) {
                // New watch
                watch.setCreatedDate(LocalDateTime.now());
                watch.setSoldCount(0);
            }
            watch.setUpdatedDate(LocalDateTime.now());

            // Save watch first to get ID
            Watch savedWatch = watchRepo.save(watch);

            // Upload main image - GHI ĐÈ
            if (mainImage != null && !mainImage.isEmpty()) {
                String imagePath = fileUploadService.uploadWatchMainImage(mainImage, savedWatch.getWatchId());

                // Create or update main image record
                WatchImage mainImg = savedWatch.getImages() != null 
                    ? savedWatch.getImages().stream()
                        .filter(img -> img.getIsPrimary())
                        .findFirst()
                        .orElse(new WatchImage())
                    : new WatchImage();

                mainImg.setWatch(savedWatch);
                mainImg.setImageUrl(imagePath);
                mainImg.setIsPrimary(true);
                watchImageRepo.save(mainImg);
            }

            // Upload gallery images - BỔ SUNG
            if (galleryImages != null && !galleryImages.isEmpty()) {
                for (MultipartFile file : galleryImages) {
                    if (!file.isEmpty()) {
                        String imagePath = fileUploadService.uploadWatchGalleryImage(file, savedWatch.getWatchId());

                        WatchImage galleryImg = new WatchImage();
                        galleryImg.setWatch(savedWatch);
                        galleryImg.setImageUrl(imagePath);
                        galleryImg.setIsPrimary(false);
                        watchImageRepo.save(galleryImg);
                    }
                }
            }

            redirectAttributes.addFlashAttribute("success", "Lưu sản phẩm thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/admin/watches";
    }

    /**
     * Delete watch
     * URL: POST /admin/watches/delete/{id}
     */
    @PostMapping("/delete/{id}")
    public String deleteWatch(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {
        try {
            Watch watch = watchRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đồng hồ"));

            // Check if watch is in any order
            boolean hasOrders = orderDetailRepo.existsByWatchWatchId(id);

            if (hasOrders) {
                // Không xóa, chỉ inactive
                watch.setIsActive(false);
                watch.setUpdatedDate(LocalDateTime.now());
                watchRepo.save(watch);

                redirectAttributes.addFlashAttribute("warning",
                        "Sản phẩm đã có trong đơn hàng. Đã chuyển sang trạng thái Inactive.");
            } else {
                // Xóa images trước
                List<WatchImage> images = watch.getImages();
                for (WatchImage img : images) {
                    try {
                        fileUploadService.deleteWatchImage(img.getImageUrl());
                    } catch (Exception e) {
                        System.err.println("Lỗi xóa image: " + e.getMessage());
                    }
                }

                // Xóa watch
                watchRepo.deleteById(id);

                redirectAttributes.addFlashAttribute("success", "Xóa sản phẩm thành công!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/admin/watches";
    }

    /**
     * Toggle active status
     * URL: POST /admin/watches/toggle-active/{id}
     */
    @PostMapping("/toggle-active/{id}")
    public String toggleActive(
            @PathVariable Integer id,
            RedirectAttributes redirectAttributes) {
        try {
            Watch watch = watchRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đồng hồ"));

            watch.setIsActive(!watch.getIsActive());
            watch.setUpdatedDate(LocalDateTime.now());
            watchRepo.save(watch);

            String status = watch.getIsActive() ? "Active" : "Inactive";
            redirectAttributes.addFlashAttribute("success", "Đã chuyển sản phẩm sang " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/watches";
    }

    /**
     * Update stock
     * URL: POST /admin/watches/update-stock/{id}
     */
    @PostMapping("/update-stock/{id}")
    public String updateStock(
            @PathVariable Integer id,
            @RequestParam Integer stockQuantity,
            RedirectAttributes redirectAttributes) {
        try {
            Watch watch = watchRepo.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đồng hồ"));

            watch.setStockQuantity(stockQuantity);
            watch.setUpdatedDate(LocalDateTime.now());
            watchRepo.save(watch);

            redirectAttributes.addFlashAttribute("success", "Cập nhật tồn kho thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/admin/watches";
    }
}
