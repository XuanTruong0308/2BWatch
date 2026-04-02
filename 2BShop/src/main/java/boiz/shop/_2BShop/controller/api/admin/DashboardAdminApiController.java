package boiz.shop._2BShop.controller.api.admin;

import boiz.shop._2BShop.controller.api.ApiDataMapper;
import boiz.shop._2BShop.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

@Tag(name = "Admin Dashboard API")
@RestController
@RequestMapping("/api/v1/admin/dashboard")
public class DashboardAdminApiController {

    @Autowired
    private DashboardService dashboardService;

    @Operation(summary = "Get admin dashboard summary")
    @GetMapping
    public ResponseEntity<Map<String, Object>> dashboard(
            @RequestParam(defaultValue = "month") String period) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("revenue", dashboardService.getRevenue(period));
            data.put("orderCount", dashboardService.getOrderCount(period));
            data.put("productCount", dashboardService.getProductCount());
            data.put("userCount", dashboardService.getUserCount());
            data.put("recentOrders", ApiDataMapper.mapOrders(dashboardService.getRecentOrders()));
            data.put("orderStatsByStatus", dashboardService.getOrderStatsByStatus());
            data.put("orderStatsByBrand", dashboardService.getOrderStatsByBrand());
            data.put("revenueChartData", dashboardService.getRevenueChartData());
            data.put("brandChartData", dashboardService.getBrandChartData());
            data.put("orderGrowth", dashboardService.getOrderGrowthPercentage());
            data.put("selectedPeriod", period);

            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> data = new HashMap<>();
            data.put("revenue", BigDecimal.ZERO);
            data.put("orderCount", 0L);
            data.put("productCount", 0L);
            data.put("userCount", 0L);
            data.put("recentOrders", new ArrayList<>());
            data.put("orderStatsByStatus", Map.of(
                    "PENDING", 0L,
                    "CONFIRMED", 0L,
                    "SHIPPING", 0L,
                    "DELIVERED", 0L,
                    "CANCELLED", 0L
            ));
            data.put("orderStatsByBrand", new HashMap<>());
            data.put("revenueChartData", Map.of("labels", new ArrayList<>(), "data", new ArrayList<>()));
            data.put("brandChartData", Map.of("labels", new ArrayList<>(), "data", new ArrayList<>()));
            data.put("orderGrowth", 0.0);
            data.put("selectedPeriod", period);

            response.put("success", false);
            response.put("message", "Có lỗi khi tải dữ liệu dashboard. Vui lòng thử lại sau.");
            response.put("data", data);
            return ResponseEntity.badRequest().body(response);
        }
    }
}
