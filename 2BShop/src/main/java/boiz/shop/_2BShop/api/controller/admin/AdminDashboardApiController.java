package boiz.shop._2BShop.api.controller.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import boiz.shop._2BShop.api.dto.DashboardDto;
import boiz.shop._2BShop.api.mapper.ApiMapper;
import boiz.shop._2BShop.api.response.ApiResponse;
import boiz.shop._2BShop.service.DashboardService;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
public class AdminDashboardApiController {

    private final DashboardService dashboardService;
    private final ApiMapper apiMapper;

    public AdminDashboardApiController(DashboardService dashboardService, ApiMapper apiMapper) {
        this.dashboardService = dashboardService;
        this.apiMapper = apiMapper;
    }

    @GetMapping
    public ApiResponse<DashboardDto> dashboard(@RequestParam(defaultValue = "month") String period) {
        return ApiResponse.success(new DashboardDto(
                dashboardService.getRevenue(period),
                dashboardService.getOrderCount(period),
                dashboardService.getProductCount(),
                dashboardService.getUserCount(),
                dashboardService.getOrderGrowthPercentage(),
                dashboardService.getOrderStatsByStatus(),
                dashboardService.getOrderStatsByBrand(),
                apiMapper.toChartSeriesDto(dashboardService.getRevenueChartData()),
                apiMapper.toChartSeriesDto(dashboardService.getBrandChartData()),
                dashboardService.getRecentOrders().stream().map(apiMapper::toOrderDto).toList()));
    }
}
