package com.firomsa.inventory.v1.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.firomsa.inventory.model.Restock;
import com.firomsa.inventory.model.Sale;
import com.firomsa.inventory.repository.RestockRepository;
import com.firomsa.inventory.repository.SaleRepository;
import com.firomsa.inventory.v1.dto.TopProductDTO;
import com.firomsa.inventory.v1.dto.TransactionSummaryDTO;
import com.firomsa.inventory.v1.dto.TransactionTrendPointDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final SaleRepository saleRepository;
    private final RestockRepository restockRepository;

    /**
     * Computes an aggregate summary of sales and restocks for the given date range.
     */
    @Transactional(readOnly = true)
    public TransactionSummaryDTO getSummary(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<Sale> sales = saleRepository.findByTimestampBetween(start, end);
        List<Restock> restocks = restockRepository.findByTimestampBetween(start, end);

        long totalSalesCount = sales.size();
        long totalUnitsSold = sales.stream()
                .mapToLong(Sale::getQuantity)
                .sum();
        BigDecimal totalSalesRevenue = sales.stream()
                .map(s -> s.getSalePrice().multiply(BigDecimal.valueOf(s.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalRestocksCount = restocks.size();
        long totalUnitsRestocked = restocks.stream()
                .mapToLong(Restock::getQuantity)
                .sum();

        return TransactionSummaryDTO.builder()
                .totalSalesCount(totalSalesCount)
                .totalSalesRevenue(totalSalesRevenue)
                .totalUnitsSold(totalUnitsSold)
                .totalRestocksCount(totalRestocksCount)
                .totalUnitsRestocked(totalUnitsRestocked)
                .startDate(startDate.toString())
                .endDate(endDate.toString())
                .build();
    }

    /**
     * Returns time-series trend data grouped by the specified granularity.
     */
    @Transactional(readOnly = true)
    public List<TransactionTrendPointDTO> getTrends(String granularity, LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<Sale> sales = saleRepository.findByTimestampBetween(start, end);
        List<Restock> restocks = restockRepository.findByTimestampBetween(start, end);

        // Group sales by period
        Map<String, List<Sale>> salesByPeriod = sales.stream()
                .collect(Collectors.groupingBy(
                        s -> toPeriodKey(s.getTimestamp(), granularity),
                        LinkedHashMap::new,
                        Collectors.toList()));

        // Group restocks by period
        Map<String, List<Restock>> restocksByPeriod = restocks.stream()
                .collect(Collectors.groupingBy(
                        r -> toPeriodKey(r.getTimestamp(), granularity),
                        LinkedHashMap::new,
                        Collectors.toList()));

        // Collect all unique periods and sort them
        var allPeriods = new java.util.TreeSet<String>();
        allPeriods.addAll(salesByPeriod.keySet());
        allPeriods.addAll(restocksByPeriod.keySet());

        // Also fill in missing periods between start and end
        fillMissingPeriods(allPeriods, startDate, endDate, granularity);

        List<TransactionTrendPointDTO> trendPoints = new ArrayList<>();

        for (String period : allPeriods) {
            List<Sale> periodSales = salesByPeriod.getOrDefault(period, List.of());
            List<Restock> periodRestocks = restocksByPeriod.getOrDefault(period, List.of());

            long salesCount = periodSales.size();
            long unitsSold = periodSales.stream().mapToLong(Sale::getQuantity).sum();
            BigDecimal salesRevenue = periodSales.stream()
                    .map(s -> s.getSalePrice().multiply(BigDecimal.valueOf(s.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long restocksCount = periodRestocks.size();
            long unitsRestocked = periodRestocks.stream().mapToLong(Restock::getQuantity).sum();

            trendPoints.add(TransactionTrendPointDTO.builder()
                    .period(period)
                    .salesCount(salesCount)
                    .salesRevenue(salesRevenue)
                    .restocksCount(restocksCount)
                    .unitsSold(unitsSold)
                    .unitsRestocked(unitsRestocked)
                    .build());
        }

        return trendPoints;
    }

    /**
     * Returns the top products by sales volume or revenue within the given date range.
     */
    @Transactional(readOnly = true)
    public List<TopProductDTO> getTopProducts(int limit, LocalDate startDate, LocalDate endDate, String sortBy) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<Sale> sales = saleRepository.findByTimestampBetween(start, end);
        List<Restock> restocks = restockRepository.findByTimestampBetween(start, end);

        // Aggregate sales by product
        Map<UUID, ProductAggregation> aggregationMap = new LinkedHashMap<>();

        for (Sale sale : sales) {
            var product = sale.getProduct();
            aggregationMap.computeIfAbsent(product.getId(),
                    id -> new ProductAggregation(id, product.getName()));
            var agg = aggregationMap.get(product.getId());
            agg.totalQuantitySold += sale.getQuantity();
            agg.totalRevenue = agg.totalRevenue.add(
                    sale.getSalePrice().multiply(BigDecimal.valueOf(sale.getQuantity())));
        }

        // Aggregate restocks by product
        for (Restock restock : restocks) {
            var product = restock.getProduct();
            aggregationMap.computeIfAbsent(product.getId(),
                    id -> new ProductAggregation(id, product.getName()));
            var agg = aggregationMap.get(product.getId());
            agg.totalTimesRestocked++;
        }

        // Sort and limit
        Comparator<ProductAggregation> comparator = "revenue".equalsIgnoreCase(sortBy)
                ? Comparator.comparing((ProductAggregation a) -> a.totalRevenue).reversed()
                : Comparator.comparingLong((ProductAggregation a) -> a.totalQuantitySold).reversed();

        return aggregationMap.values().stream()
                .sorted(comparator)
                .limit(limit)
                .map(agg -> TopProductDTO.builder()
                        .productId(agg.productId)
                        .productName(agg.productName)
                        .totalQuantitySold(agg.totalQuantitySold)
                        .totalRevenue(agg.totalRevenue)
                        .totalTimesRestocked(agg.totalTimesRestocked)
                        .build())
                .toList();
    }

    // --- Helper methods ---

    private String toPeriodKey(LocalDateTime timestamp, String granularity) {
        LocalDate date = timestamp.toLocalDate();
        return switch (granularity.toUpperCase()) {
            case "WEEKLY" -> date.get(IsoFields.WEEK_BASED_YEAR) + "-W"
                    + String.format("%02d", date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
            case "MONTHLY" -> date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            default -> date.toString(); // DAILY: yyyy-MM-dd
        };
    }

    private void fillMissingPeriods(java.util.TreeSet<String> periods,
            LocalDate startDate, LocalDate endDate, String granularity) {
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            periods.add(toPeriodKey(current.atStartOfDay(), granularity));
            current = switch (granularity.toUpperCase()) {
                case "WEEKLY" -> current.plusWeeks(1);
                case "MONTHLY" -> current.plusMonths(1);
                default -> current.plusDays(1);
            };
        }
    }

    /**
     * Internal helper to accumulate per-product aggregation data.
     */
    private static class ProductAggregation {
        final UUID productId;
        final String productName;
        long totalQuantitySold;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalTimesRestocked;

        ProductAggregation(UUID productId, String productName) {
            this.productId = productId;
            this.productName = productName;
        }
    }
}
