package boiz.shop._2BShop.service;

import boiz.shop._2BShop.entity.Order;
import boiz.shop._2BShop.respository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WatchRepository watchRepository;

    @Mock
    private UserRepository userRepository;
    
    @Mock
    private OrderDetailRepository orderDetailRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    public void testGetRevenue_WithValidPeriod() {
        when(orderRepository.sumTotalAmountByStatus(any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(BigDecimal.valueOf(5000000));

        BigDecimal revenue = dashboardService.getRevenue("week");

        assertNotNull(revenue);
        assertEquals(BigDecimal.valueOf(5000000), revenue);
        verify(orderRepository, times(1))
                .sumTotalAmountByStatus(any(LocalDateTime.class), any(LocalDateTime.class), anyList());
    }

    @Test
    public void testGetRevenue_ReturnsZeroWhenNull() {
        when(orderRepository.sumTotalAmountByStatus(any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(null);

        BigDecimal revenue = dashboardService.getRevenue("month");

        assertNotNull(revenue);
        assertEquals(BigDecimal.ZERO, revenue);
    }

    @Test
    public void testGetOrderCount() {
        List<Order> mockOrders = Arrays.asList(new Order(), new Order(), new Order());
        when(orderRepository.findByOrderDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(mockOrders);

        Long count = dashboardService.getOrderCount("month");

        assertEquals(3L, count);
        verify(orderRepository, times(1)).findByOrderDateBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    public void testGetProductCount() {
        when(watchRepository.countByIsActiveTrue()).thenReturn(75L);

        Long count = dashboardService.getProductCount();

        assertEquals(75L, count);
        verify(watchRepository, times(1)).countByIsActiveTrue();
    }

    @Test
    public void testGetUserCount_ExcludesAdmins() {
        when(userRepository.count()).thenReturn(260L);
        when(userRepository.countByRoleName("ADMIN")).thenReturn(10L);

        Long count = dashboardService.getUserCount();

        assertEquals(250L, count);
        verify(userRepository, times(1)).count();
        verify(userRepository, times(1)).countByRoleName("ADMIN");
    }

    @Test
    public void testGetOrderStatsByStatus() {
        when(orderRepository.countByOrderStatus("PENDING")).thenReturn(20L);
        when(orderRepository.countByOrderStatus("CONFIRMED")).thenReturn(15L);
        when(orderRepository.countByOrderStatus("SHIPPING")).thenReturn(10L);
        when(orderRepository.countByOrderStatus("DELIVERED")).thenReturn(30L);
        when(orderRepository.countByOrderStatus("CANCELLED")).thenReturn(5L);

        Map<String, Long> stats = dashboardService.getOrderStatsByStatus();

        assertNotNull(stats);
        assertEquals(5, stats.size());
        assertEquals(20L, stats.get("PENDING"));
        assertEquals(15L, stats.get("CONFIRMED"));
        verify(orderRepository, times(5)).countByOrderStatus(anyString());
    }

    @Test
    public void testGetRevenueChartData_ReturnsValidStructure() {
        when(orderRepository.sumTotalAmountByDateRangeAndStatus(
                any(LocalDateTime.class), any(LocalDateTime.class), anyList()))
                .thenReturn(BigDecimal.valueOf(1000000));

        Map<String, Object> chartData = dashboardService.getRevenueChartData();

        assertNotNull(chartData);
        assertTrue(chartData.containsKey("labels"));
        assertTrue(chartData.containsKey("data"));
        
        @SuppressWarnings("unchecked")
        List<String> labels = (List<String>) chartData.get("labels");
        assertEquals(12, labels.size());
        
        verify(orderRepository, times(12)).sumTotalAmountByDateRangeAndStatus(
                any(LocalDateTime.class), any(LocalDateTime.class), anyList());
    }
    
    @Test
    public void testGetRecentOrders() {
        List<Order> mockOrders = Arrays.asList(new Order(), new Order());
        when(orderRepository.findTop10ByOrderByOrderDateDesc()).thenReturn(mockOrders);

        List<Order> orders = dashboardService.getRecentOrders();

        assertNotNull(orders);
        assertEquals(2, orders.size());
        verify(orderRepository, times(1)).findTop10ByOrderByOrderDateDesc();
    }
}

