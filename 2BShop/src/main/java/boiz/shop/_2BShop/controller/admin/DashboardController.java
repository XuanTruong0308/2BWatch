package boiz.shop._2BShop.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import boiz.shop._2BShop.service.DashboardService;

/**
 * Admin Dashboard Controller
 * Handles admin dashboard statistics and charts
 * URL: /admin hoặc /admin/dashboard
 */
@Controller
@RequestMapping("/admin")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Admin Dashboard Page
     * URL: /admin hoặc /admin/dashboard
     */
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(
        @RequestParam(defaultValue = "month") String period,
        Model model
    ) {
        try {
            // Statistics cards - with default values
            model.addAttribute("revenue", dashboardService.getRevenue(period));
            model.addAttribute("orderCount", dashboardService.getOrderCount(period));
            model.addAttribute("productCount", dashboardService.getProductCount());
            model.addAttribute("userCount", dashboardService.getUserCount());
            
            // Recent orders - ensure not null
            model.addAttribute("recentOrders", dashboardService.getRecentOrders());
            
            // Order stats by status - ensure not null
            model.addAttribute("orderStatsByStatus", dashboardService.getOrderStatsByStatus());
            
            // Order stats by brand - ensure not null
            model.addAttribute("orderStatsByBrand", dashboardService.getOrderStatsByBrand());
            
            // Chart data - ensure not null
            model.addAttribute("revenueChartData", dashboardService.getRevenueChartData());
            model.addAttribute("brandChartData", dashboardService.getBrandChartData());
            
            // Order growth - ensure not null
            model.addAttribute("orderGrowth", dashboardService.getOrderGrowthPercentage());
            
            // Selected period
            model.addAttribute("selectedPeriod", period);
            
        } catch (Exception e) {
            System.err.println("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
            
            // Set default values on error to prevent template crash
            model.addAttribute("revenue", java.math.BigDecimal.ZERO);
            model.addAttribute("orderCount", 0L);
            model.addAttribute("productCount", 0L);
            model.addAttribute("userCount", 0L);
            model.addAttribute("recentOrders", new java.util.ArrayList<>());
            
            java.util.Map<String, Long> emptyStats = new java.util.HashMap<>();
            emptyStats.put("PENDING", 0L);
            emptyStats.put("CONFIRMED", 0L);
            emptyStats.put("SHIPPING", 0L);
            emptyStats.put("DELIVERED", 0L);
            emptyStats.put("CANCELLED", 0L);
            model.addAttribute("orderStatsByStatus", emptyStats);
            model.addAttribute("orderStatsByBrand", new java.util.HashMap<>());
            
            java.util.Map<String, Object> emptyChartData = new java.util.HashMap<>();
            emptyChartData.put("labels", new java.util.ArrayList<>());
            emptyChartData.put("data", new java.util.ArrayList<>());
            model.addAttribute("revenueChartData", emptyChartData);
            model.addAttribute("brandChartData", emptyChartData);
            
            model.addAttribute("orderGrowth", 0.0);
            model.addAttribute("selectedPeriod", period);
            model.addAttribute("error", "Có lỗi khi tải dữ liệu dashboard. Vui lòng thử lại sau.");
        }
        
        return "admin/dashboard";
    }
}
