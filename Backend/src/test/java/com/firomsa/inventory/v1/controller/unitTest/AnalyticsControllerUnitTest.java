package com.firomsa.inventory.v1.controller.unitTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import com.firomsa.inventory.support.TestCacheConfig;
import com.firomsa.inventory.v1.controller.AnalyticsController;
import com.firomsa.inventory.v1.dto.TopProductDTO;
import com.firomsa.inventory.v1.dto.TransactionSummaryDTO;
import com.firomsa.inventory.v1.dto.TransactionTrendPointDTO;
import com.firomsa.inventory.v1.service.AnalyticsService;

@WebMvcTest(AnalyticsController.class)
@Import(TestCacheConfig.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = "SCOPE_ADMIN")
public class AnalyticsControllerUnitTest {

    @MockitoBean
    private AnalyticsService analyticsService;

    @Autowired
    private MockMvcTester mockMvc;

    private static final String BASE_URL = "/api/v1/admin/analytics";

    @Test
    @DisplayName("should get transaction summary")
    void shouldGetTransactionSummary() {
        // Arrange
        TransactionSummaryDTO summaryDTO = TransactionSummaryDTO.builder()
                .totalSalesCount(10)
                .totalSalesRevenue(new BigDecimal("1500.00"))
                .totalUnitsSold(25)
                .totalRestocksCount(3)
                .totalUnitsRestocked(50)
                .startDate("2026-07-01")
                .endDate("2026-07-29")
                .build();

        when(analyticsService.getSummary(LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-29")))
                .thenReturn(summaryDTO);

        // Act
        MvcTestResult result = mockMvc.get()
                .uri(BASE_URL + "/summary?startDate=2026-07-01&endDate=2026-07-29")
                .exchange();

        // Assert
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.totalSalesCount").isEqualTo(10);
        assertThat(result).bodyJson().extractingPath("$.totalUnitsSold").isEqualTo(25);
        assertThat(result).bodyJson().extractingPath("$.totalSalesRevenue").isEqualTo(1500.0);
        assertThat(result).bodyJson().extractingPath("$.totalRestocksCount").isEqualTo(3);
        assertThat(result).bodyJson().extractingPath("$.totalUnitsRestocked").isEqualTo(50);
        assertThat(result).bodyJson().extractingPath("$.startDate").isEqualTo("2026-07-01");
        assertThat(result).bodyJson().extractingPath("$.endDate").isEqualTo("2026-07-29");

        verify(analyticsService).getSummary(LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-29"));
    }

    @Test
    @DisplayName("should get transaction trends")
    void shouldGetTransactionTrends() {
        // Arrange
        TransactionTrendPointDTO point = TransactionTrendPointDTO.builder()
                .period("2026-07-29")
                .salesCount(5)
                .salesRevenue(new BigDecimal("500.00"))
                .restocksCount(1)
                .unitsSold(10)
                .unitsRestocked(20)
                .build();

        when(analyticsService.getTrends(anyString(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(point));

        // Act
        MvcTestResult result = mockMvc.get()
                .uri(BASE_URL + "/trends?granularity=DAILY&startDate=2026-07-01&endDate=2026-07-29")
                .exchange();

        // Assert
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$[0].period").isEqualTo("2026-07-29");
        assertThat(result).bodyJson().extractingPath("$[0].salesCount").isEqualTo(5);
        assertThat(result).bodyJson().extractingPath("$[0].salesRevenue").isEqualTo(500.0);
        assertThat(result).bodyJson().extractingPath("$[0].unitsSold").isEqualTo(10);
        assertThat(result).bodyJson().extractingPath("$[0].unitsRestocked").isEqualTo(20);

        verify(analyticsService).getTrends("DAILY", LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-29"));
    }

    @Test
    @DisplayName("should get top products")
    void shouldGetTopProducts() {
        // Arrange
        TopProductDTO topProduct = TopProductDTO.builder()
                .productId(UUID.randomUUID())
                .productName("Wireless Mouse")
                .totalQuantitySold(15)
                .totalRevenue(new BigDecimal("750.00"))
                .totalTimesRestocked(2)
                .build();

        when(analyticsService.getTopProducts(anyInt(), any(LocalDate.class), any(LocalDate.class), anyString()))
                .thenReturn(List.of(topProduct));

        // Act
        MvcTestResult result = mockMvc.get()
                .uri(BASE_URL + "/top-products?limit=5&startDate=2026-07-01&endDate=2026-07-29&sortBy=revenue")
                .exchange();

        // Assert
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$[0].productName").isEqualTo("Wireless Mouse");
        assertThat(result).bodyJson().extractingPath("$[0].totalQuantitySold").isEqualTo(15);
        assertThat(result).bodyJson().extractingPath("$[0].totalRevenue").isEqualTo(750.0);
        assertThat(result).bodyJson().extractingPath("$[0].totalTimesRestocked").isEqualTo(2);

        verify(analyticsService).getTopProducts(5, LocalDate.parse("2026-07-01"), LocalDate.parse("2026-07-29"), "revenue");
    }
}
