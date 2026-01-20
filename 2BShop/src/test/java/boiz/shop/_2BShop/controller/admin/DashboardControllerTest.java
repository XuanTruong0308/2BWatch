package boiz.shop._2BShop.controller.admin;

import boiz.shop._2BShop.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DashboardService dashboardService;
    
    @InjectMocks
    private DashboardController dashboardController;
    
    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(dashboardController).build();
    }

    @Test
    public void testDashboardPage_Success() throws Exception {
        when(dashboardService.getRevenue(anyString())).thenReturn(BigDecimal.valueOf(10000000));
        when(dashboardService.getOrderCount(anyString())).thenReturn(100L);
        when(dashboardService.getProductCount()).thenReturn(50L);
        when(dashboardService.getUserCount()).thenReturn(200L);
        when(dashboardService.getRecentOrders()).thenReturn(Collections.emptyList());
        
        Map<String, Long> orderStats = new HashMap<>();
        orderStats.put("PENDING", 10L);
        when(dashboardService.getOrderStatsByStatus()).thenReturn(orderStats);
        
        Map<String, Object> revenueChart = new HashMap<>();
        revenueChart.put("labels", Arrays.asList("Jan"));
        revenueChart.put("data", Arrays.asList(1000000));
        when(dashboardService.getRevenueChartData()).thenReturn(revenueChart);
        
        Map<String, Object> brandChart = new HashMap<>();
        brandChart.put("labels", Arrays.asList("Rolex"));
        brandChart.put("data", Arrays.asList(30));
        when(dashboardService.getBrandChartData()).thenReturn(brandChart);
        
        when(dashboardService.getOrderGrowthPercentage()).thenReturn(15.5);

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"));
    }
}