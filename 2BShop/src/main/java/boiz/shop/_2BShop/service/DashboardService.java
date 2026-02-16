package boiz.shop._2BShop.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.entity.OrderDetail;
import boiz.shop._2BShop.respository.OrderDetailRepository;
import boiz.shop._2BShop.respository.OrderRepository;
import boiz.shop._2BShop.respository.UserRepository;
import boiz.shop._2BShop.respository.WatchRepository;

@Service
public class DashboardService {

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private WatchRepository watchRepo;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    // Doanh thu theo khoảng thời gian
    public BigDecimal getRevenue(String period) {
        LocalDateTime startDate = getStartDate(period);
        LocalDateTime endDate = LocalDateTime.now();

        List<String> validStatuses = Arrays.asList("DELIVERED", "COMPLETED");

        BigDecimal revenue = orderRepo.sumTotalAmountByStatus(
                startDate, endDate, validStatuses);

        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    // Đếm số đơn hàng theo khoảng thời gian
    public Long getOrderCount(String period) {
        LocalDateTime startDate = getStartDate(period);
        LocalDateTime endDate = LocalDateTime.now();

        List<Order> orders = orderRepo.findByOrderDateBetween(startDate, endDate);
        return (long) orders.size();
    }

    // lấy sản phẩm được active
    public Long getProductCount() {
        return watchRepo.countByIsActiveTrue();
    }

    // Lấy số lượng user
    public Long getUserCount() {
        long totalUsers = userRepo.count();

        long adminCount = userRepo.countByRoleName("ADMIN");

        return totalUsers - adminCount;
    }

    // thống kê trạng thái đơn
    public List<Order> getRecentOrders() {
        return orderRepo.findTop10ByOrderByOrderDateDesc();
    }

    public Map<String, Long> getOrderStatsByStatus() {
        Map<String, Long> stats = new HashMap<>();

        stats.put("PENDING", orderRepo.countByOrderStatus("PENDING"));
        stats.put("CONFIRMED", orderRepo.countByOrderStatus("CONFIRMED"));
        stats.put("SHIPPING", orderRepo.countByOrderStatus("SHIPPING"));
        stats.put("DELIVERED", orderRepo.countByOrderStatus("DELIVERED"));
        stats.put("CANCELLED", orderRepo.countByOrderStatus("CANCELLED"));

        return stats;
    }

    // Thống kế lượt order theo brand
    private Map<String, Long> getOrderStatsByBrand() {
        Map<String, Long> stats = new LinkedHashMap<>();

        List<OrderDetail> allDetails = orderDetailRepository.findAll();

        for (OrderDetail detail : allDetails) {
            String brandName = detail.getWatch().getBrand().getBrandName();
            stats.put(brandName, stats.getOrDefault(brandName, 0L) + 1);
        }

        return stats.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(LinkedHashMap::new,
                        (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                        LinkedHashMap::putAll);
    }

    public Map<String, Object> getRevenueChartData() {
        Map<String, Object> chartData = new HashMap<>();

        List<String> labels = new ArrayList<>();
        List<BigDecimal> data = new ArrayList<>();

        LocalDate now = LocalDate.now();

        // 12 tháng gần nhất
        for (int i = 11; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            LocalDate monthStart = monthDate.withDayOfMonth(1);
            LocalDate monthEnd = monthDate.with(TemporalAdjusters.lastDayOfMonth());

            // Label: "Jan 2026"
            String label = monthDate.getMonth().toString().substring(0, 3) + " " + monthDate.getYear();
            labels.add(label);

            // Renvue tháng đó
            BigDecimal revenue = orderRepo.sumTotalAmountByDateRangeAndStatus(
                    monthStart.atStartOfDay(),
                    monthEnd.atTime(23, 59, 59),
                    Arrays.asList("DELIVERED", "COMPLETED"));

            data.add(revenue != null ? revenue : BigDecimal.ZERO);
        }

        chartData.put("labels", labels);
        chartData.put("data", data);

        return chartData;
    }

    public Map<String, Object> getBrandChartData() {
        Map<String, Long> brandStats = getOrderStatsByBrand();

        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", new ArrayList<>(brandStats.keySet()));
        chartData.put("data", new ArrayList<>(brandStats.values()));

        return chartData;
    }

    private LocalDateTime getStartDate(String period) {
        LocalDate today = LocalDate.now();

        switch (period.toLowerCase()) {
            case "today":
                return today.atStartOfDay();
            case "week":
                return today.minusWeeks(1).atStartOfDay();
            case "month":
                return today.minusMonths(1).atStartOfDay();
            case "quarter":
                return today.minusMonths(3).atStartOfDay();
            case "year":
                return today.minusYears(1).atStartOfDay();
            case "all":
                return LocalDateTime.of(2000, 1, 1, 0, 0);
            default:
                return today.atStartOfDay();
        }
    }

    public Double getOrderGrowthPercentage() {
        LocalDate now = LocalDate.now();
        LocalDate lastMonthStart = now.minusMonths(1).withDayOfMonth(1);
        LocalDate lastMonthEnd = now.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

        LocalDate thisMonthStart = now.withDayOfMonth(1);
        LocalDate thisMonthEnd = now;

        long lastMonthCount = orderRepo.findByOrderDateBetween(
                lastMonthStart.atStartOfDay(),
                lastMonthEnd.atTime(LocalTime.MAX)).size();

        long thisMonthCount = orderRepo.findByOrderDateBetween(
                thisMonthStart.atStartOfDay(),
                thisMonthEnd.atTime(LocalTime.MAX)).size();

        if (lastMonthCount == 0) {
            return thisMonthCount > 0 ? 100.0 : 0.0;
        }

        return ((double) (thisMonthCount - lastMonthCount) / lastMonthCount) * 100;
    }
}
