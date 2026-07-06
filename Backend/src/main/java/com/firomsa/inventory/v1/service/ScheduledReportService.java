package com.firomsa.inventory.v1.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.firomsa.inventory.config.AdminConfig;
import com.firomsa.inventory.model.Category;
import com.firomsa.inventory.model.Product;
import com.firomsa.inventory.model.Sale;
import com.firomsa.inventory.repository.CategoryRepository;
import com.firomsa.inventory.repository.ProductRepository;
import com.firomsa.inventory.repository.SaleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledReportService {

    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final CategoryRepository categoryRepository;
    private final EmailService emailService;
    private final AdminConfig adminConfig;

    @Scheduled(cron = "0 0 8 * * *", zone = "Africa/Addis_Ababa")
    public void sendDailyReport() {
        log.info("Generating daily inventory report");

        String report = generateReport();
        emailService.sendDailyReport(adminConfig.getEmail(), report);

        log.info("Daily inventory report sent to admin");
    }

    String generateReport() {
        long totalProducts = productRepository.count();
        BigDecimal totalInventoryValue = productRepository.calculateTotalInventoryValue();
        long lowStockCount = productRepository.countByQuantityLessThanLowStockThreshold();
        long outOfStockCount = productRepository.countByQuantityEquals(0);

        // Yesterday's sales summary
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startOfYesterday = yesterday.atStartOfDay(ZoneId.of("Africa/Addis_Ababa")).toLocalDateTime();
        LocalDateTime endOfYesterday = yesterday.atTime(LocalTime.MAX).atZone(ZoneId.of("Africa/Addis_Ababa"))
                .toLocalDateTime();

        List<Sale> yesterdaySales = saleRepository.findByTimestampBetween(startOfYesterday, endOfYesterday);
        int yesterdaySalesCount = yesterdaySales.size();
        BigDecimal yesterdayRevenue = yesterdaySales.stream()
                .map(sale -> sale.getSalePrice().multiply(BigDecimal.valueOf(sale.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder report = new StringBuilder();
        report.append("Daily Inventory Report\n");
        report.append("======================\n\n");
        report.append("Date: ").append(LocalDate.now()).append("\n\n");

        report.append("INVENTORY OVERVIEW\n");
        report.append("------------------\n");
        report.append("Total Products: ").append(totalProducts).append("\n");
        report.append("Total Inventory Value: $").append(totalInventoryValue).append("\n");
        report.append("Low Stock Products: ").append(lowStockCount).append("\n");
        report.append("Out of Stock Products: ").append(outOfStockCount).append("\n\n");

        report.append("YESTERDAY'S ACTIVITY (").append(yesterday).append(")\n");
        report.append("------------------------\n");
        report.append("Sales Count: ").append(yesterdaySalesCount).append("\n");
        report.append("Total Revenue: $").append(yesterdayRevenue).append("\n");

        // Category breakdown
        report.append("\nCATEGORY BREAKDOWN\n");
        report.append("------------------\n");
        List<Category> categories = categoryRepository.findAll();
        for (Category category : categories) {
            BigDecimal categoryValue = productRepository.calculateInventoryValueByCategory(category.getId());
            if (categoryValue.compareTo(BigDecimal.ZERO) > 0) {
                report.append(category.getName()).append(": $").append(categoryValue).append("\n");
            }
        }

        if (lowStockCount > 0) {
            report.append("\nLOW STOCK ALERT\n");
            report.append("---------------\n");
            List<Product> lowStockProducts = productRepository.findByQuantityLessThanLowStockThreshold();
            for (Product product : lowStockProducts) {
                report.append("- ").append(product.getName())
                        .append(" (").append(product.getSku()).append("): ")
                        .append(product.getQuantity()).append(" / ")
                        .append(product.getLowStockThreshold()).append("\n");
            }
        }

        return report.toString();
    }
}
