package boiz.shop._2BShop.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import boiz.shop._2BShop.dto.RegisterDTO;
import boiz.shop._2BShop.entity.User;
import boiz.shop._2BShop.entity.Watch;
import boiz.shop._2BShop.service.UserService;
import boiz.shop._2BShop.service.WatchService;
import boiz.shop._2BShop.service.MailService;

/**
 * ========================================
 * PUBLIC CONTROLLER - TẤT CẢ CHỨC NĂNG CÔNG KHAI
 * ========================================
 * Bao gồm:
 * 1. Home Page - Trang chủ
 * 2. Product Listing & Detail - Danh sách & chi tiết sản phẩm
 * 3. Authentication - Đăng ký, đăng nhập, xác thực email
 * 4. About, Contact - Các trang thông tin
 */
@Controller
public class PublicController {

    @Autowired
    private WatchService watchService;

    @Autowired
    private UserService userService;

    @Autowired
    private MailService mailService;

    // ========================================
    // 1. HOME PAGE - TRANG CHỦ
    // ========================================

    /**
     * Trang chủ - Hiển thị sản phẩm nổi bật
     */
    @GetMapping("/")
    public String homePage(Model model) {
        try {
            // Sản phẩm bán chạy nhất (top 3)
            List<Watch> bestSellers = watchService.getTop3BestSellers();
            model.addAttribute("bestSellers", bestSellers);

            // Sản phẩm mới nhất (top 3)
            List<Watch> newestProducts = watchService.getTop3Newest();
            model.addAttribute("newestProducts", newestProducts);

            // Sản phẩm giảm giá nhiều nhất (top 3)
            List<Watch> biggestDiscounts = watchService.getTop3BiggestDiscount();
            model.addAttribute("biggestDiscounts", biggestDiscounts);

        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải dữ liệu trang chủ: " + e.getMessage());
        }

        return "public/index";
    }

    // ========================================
    // 2. PRODUCT LISTING & DETAIL
    // ========================================

    /**
     * Danh sách tất cả sản phẩm với search, filter
     */
    @GetMapping("/watches")
    public String allProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            Model model) {
        try {
            Pageable pageable;

            // Sort logic
            if ("price-asc".equals(sortBy)) {
                pageable = PageRequest.of(page, size, Sort.by("price").ascending());
            } else if ("price-desc".equals(sortBy)) {
                pageable = PageRequest.of(page, size, Sort.by("price").descending());
            } else if ("newest".equals(sortBy)) {
                pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
            } else {
                pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
            }

            Page<Watch> watchPage;
            String pageTitle = "Tất cả sản phẩm";

            // Filter logic
            if (search != null && !search.trim().isEmpty()) {
                watchPage = watchService.search(search, pageable);
                pageTitle = "Tìm kiếm: " + search;
                model.addAttribute("search", search);

            } else if (category != null && !category.trim().isEmpty()) {
                watchPage = watchService.findByCategory(category, pageable);
                pageTitle = "Danh mục: " + category;
                model.addAttribute("category", category);

            } else if (brand != null && !brand.trim().isEmpty()) {
                watchPage = watchService.findByBrand(brand, pageable);
                pageTitle = "Thương hiệu: " + brand;
                model.addAttribute("brand", brand);

            } else if (priceRange != null && !priceRange.trim().isEmpty()) {
                watchPage = watchService.findByPriceRange(priceRange, pageable);
                pageTitle = "Khoảng giá: " + getPriceRangeLabel(priceRange);
                model.addAttribute("priceRange", priceRange);

            } else {
                // Chỉ hiển thị sản phẩm còn hàng
                watchPage = watchService.findActiveProducts(pageable);
            }

            model.addAttribute("watches", watchPage.getContent());
            model.addAttribute("pageTitle", pageTitle);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", watchPage.getTotalPages());
            model.addAttribute("totalItems", watchPage.getTotalElements());
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("baseUrl", "/watches");

        } catch (Exception e) {
            model.addAttribute("error", "Lỗi khi tải danh sách sản phẩm: " + e.getMessage());
            model.addAttribute("watches", List.of());
            model.addAttribute("pageTitle", "Sản phẩm");
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalItems", 0L);
        }

        return "public/products";
    }

    /**
     * Trang sản phẩm mới nhất
     */
    @GetMapping("/watches/newest")
    public String newestProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            Model model) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Watch> watchPage = watchService.findNewestProducts(pageable);

            System.out.println("=== DEBUG NEWEST PRODUCTS ===");
            System.out.println("Total elements: " + watchPage.getTotalElements());
            System.out.println("Total pages: " + watchPage.getTotalPages());
            System.out.println("Current page: " + page);
            System.out.println("Content size: " + watchPage.getContent().size());
            
            model.addAttribute("watches", watchPage.getContent());
            model.addAttribute("pageTitle", "Sản phẩm mới nhất");
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", watchPage.getTotalPages());
            model.addAttribute("totalItems", watchPage.getTotalElements());
            model.addAttribute("baseUrl", "/watches/newest");

            return "public/products-newest";

        } catch (Exception e) {
            System.err.println("ERROR in newestProducts: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Lỗi khi tải sản phẩm mới nhất: " + e.getMessage());
            model.addAttribute("watches", List.of());
            model.addAttribute("pageTitle", "Sản phẩm mới nhất");
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalItems", 0L);
            return "public/products-newest";
        }
    }

    /**
     * Trang sản phẩm giảm giá
     */
    @GetMapping("/watches/discount")
    public String discountProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            Model model) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Watch> watchPage = watchService.findDiscountProducts(pageable);

            System.out.println("=== DEBUG DISCOUNT PRODUCTS ===");
            System.out.println("Total elements: " + watchPage.getTotalElements());
            System.out.println("Total pages: " + watchPage.getTotalPages());
            System.out.println("Current page: " + page);
            System.out.println("Content size: " + watchPage.getContent().size());
            if (!watchPage.getContent().isEmpty()) {
                System.out.println("First product: " + watchPage.getContent().get(0).getWatchName() + 
                    " - Discount: " + watchPage.getContent().get(0).getDiscountPercent());
            }
            
            model.addAttribute("watches", watchPage.getContent());
            model.addAttribute("pageTitle", "Sản phẩm giảm giá");
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", watchPage.getTotalPages());
            model.addAttribute("totalItems", watchPage.getTotalElements());
            model.addAttribute("baseUrl", "/watches/discount");

            return "public/products-discount";

        } catch (Exception e) {
            System.err.println("ERROR in discountProducts: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Lỗi khi tải sản phẩm giảm giá: " + e.getMessage());
            model.addAttribute("watches", List.of());
            model.addAttribute("pageTitle", "Sản phẩm giảm giá");
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("totalItems", 0L);
            return "public/products-discount";
        }
    }

    /**
     * Chi tiết sản phẩm
     */
    @GetMapping("/watches/{id}")
    public String productDetail(@PathVariable Integer id, Model model,
            RedirectAttributes redirectAttributes) {
        try {
            Watch watch = watchService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

            // Sản phẩm liên quan (cùng brand)
            List<Watch> relatedWatches = watchService.findRelatedProducts(watch, 4);

            model.addAttribute("watch", watch);
            model.addAttribute("relatedWatches", relatedWatches);

            return "public/product-detail";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/watches";
        }
    }

    /**
     * Helper: Convert price range code to label
     */
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

    // ========================================
    // 3. AUTHENTICATION - ĐĂNG KÝ, ĐĂNG NHẬP
    // ========================================

    /**
     * Trang đăng nhập
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String logout,
            Model model) {
        if (error != null) {
            model.addAttribute("error", "Email hoặc mật khẩu không đúng!");
        }

        if (logout != null) {
            model.addAttribute("success", "Đăng xuất thành công!");
        }

        return "public/login";
    }

    /**
     * Trang đăng ký
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerDTO", new RegisterDTO());
        return "public/register";
    }

    /**
     * Xử lý đăng ký
     */
    @PostMapping("/register")
    public String processRegister(
            @ModelAttribute("registerDTO") RegisterDTO dto,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {
        try {
            // Validate
            if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
                throw new RuntimeException("Email không được để trống!");
            }

            if (dto.getPassword() == null || dto.getPassword().length() < 6) {
                throw new RuntimeException("Mật khẩu phải có ít nhất 6 ký tự!");
            }

            // Kiểm tra mật khẩu khớp nhau
            if (!dto.getPassword().equals(dto.getConfirmPassword())) {
                throw new RuntimeException("Mật khẩu xác nhận không khớp!");
            }

            // Kiểm tra email đã tồn tại
            if (userService.existsByEmail(dto.getEmail())) {
                throw new RuntimeException("Email đã được sử dụng!");
            }

            // Đăng ký user
            userService.registerUser(dto);

            redirectAttributes.addFlashAttribute("success",
                    "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.");

            return "redirect:/login";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("registerDTO", dto);
            return "public/register";
        }
    }

    /**
     * Trang xác thực email (form nhập email + token)
     */
    @GetMapping("/confirm-register")
    public String confirmRegisterPage(
            @RequestParam String token,
            Model model) {
        model.addAttribute("token", token);
        return "public/verify-email";
    }

    /**
     * Xử lý xác thực email
     */
    @PostMapping("/confirm-register")
    public String processConfirmRegister(
            @RequestParam String email,
            @RequestParam String token,
            RedirectAttributes redirectAttributes) {
        try {
            // Validate token
            boolean isValid = userService.verifyEmailToken(email, token);

            if (!isValid) {
                throw new RuntimeException("Token không hợp lệ hoặc đã hết hạn!");
            }

            // Activate user
            userService.activateUser(email);

            redirectAttributes.addFlashAttribute("success",
                    "Xác thực email thành công! Bạn có thể đăng nhập ngay bây giờ.");

            return "redirect:/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/confirm-register?token=" + token;
        }
    }

    /**
     * Gửi lại email xác thực
     */
    @PostMapping("/resend-verification")
    public String resendVerification(
            @RequestParam String email,
            RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với email này!"));

            if (user.getIsEnabled()) {
                throw new RuntimeException("Tài khoản đã được kích hoạt!");
            }

            // Tạo token mới và gửi email
            userService.resendVerificationEmail(email);

            redirectAttributes.addFlashAttribute("success",
                    "Email xác thực đã được gửi lại! Vui lòng kiểm tra hộp thư.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/login";
    }

    // ========================================
    // 4. ABOUT, CONTACT - CÁC TRANG THÔNG TIN
    // ========================================

    /**
     * Trang giới thiệu
     */
    @GetMapping("/about")
    public String aboutPage() {
        return "public/about";
    }

    /**
     * Trang liên hệ
     */
    @GetMapping("/contact")
    public String contactPage() {
        return "public/contact";
    }

    /**
     * Xử lý form liên hệ
     */
    @PostMapping("/contact")
    public String processContact(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String subject,
            @RequestParam String message,
            RedirectAttributes redirectAttributes) {
        try {
            // Gửi email contact
            mailService.sendContactEmail(name, email, subject, message);

            redirectAttributes.addFlashAttribute("success",
                    "Gửi thông tin liên hệ thành công! Chúng tôi sẽ phản hồi trong thời gian sớm nhất.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/contact";
    }

    /**
     * Trang chính sách
     */
    @GetMapping("/policy")
    public String policyPage() {
        return "public/policy";
    }

    /**
     * Trang điều khoản sử dụng
     */
    @GetMapping("/terms")
    public String termsPage() {
        return "public/terms";
    }

    /**
     * Trang FAQ
     */
    @GetMapping("/faq")
    public String faqPage() {
        return "public/faq";
    }
}
