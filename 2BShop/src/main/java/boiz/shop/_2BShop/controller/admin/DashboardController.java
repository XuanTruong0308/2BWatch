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
        // Statistics cards
        model.addAttribute("revenue", dashboardService.getRevenue(period));
        model.addAttribute("orderCount", dashboardService.getOrderCount(period));
        model.addAttribute("productCount", dashboardService.getProductCount());
        model.addAttribute("userCount", dashboardService.getUserCount());
        
        // Recent orders
        model.addAttribute("recentOrders", dashboardService.getRecentOrders());
        
        // Order stats by status
        model.addAttribute("orderStatsByStatus", dashboardService.getOrderStatsByStatus());
        
        // Order stats by brand
        model.addAttribute("orderStatsByBrand", dashboardService.getOrderStatsByStatus());
        
        // Chart data
        model.addAttribute("revenueChartData", dashboardService.getRevenueChartData());
        model.addAttribute("brandChartData", dashboardService.getBrandChartData());
        
        // Order growth
        model.addAttribute("orderGrowth", dashboardService.getOrderGrowthPercentage());
        
        // Selected period
        model.addAttribute("selectedPeriod", period);
        
        return "admin/dashboard";
    }
}
