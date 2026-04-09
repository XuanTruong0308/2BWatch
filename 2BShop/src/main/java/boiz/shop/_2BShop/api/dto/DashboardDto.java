package boiz.shop._2BShop.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardDto(
        BigDecimal revenue,
        Long orderCount,
        Long productCount,
        Long userCount,
        Double orderGrowth,
        Map<String, Long> orderStatsByStatus,
        Map<String, Long> orderStatsByBrand,
        ChartSeriesDto revenueChart,
        ChartSeriesDto brandChart,
        List<OrderDto> recentOrders) {
}
