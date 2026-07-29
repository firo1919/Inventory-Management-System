package com.firomsa.inventory.v1.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.firomsa.inventory.v1.dto.TopProductDTO;
import com.firomsa.inventory.v1.dto.TransactionSummaryDTO;
import com.firomsa.inventory.v1.dto.TransactionTrendPointDTO;
import com.firomsa.inventory.v1.service.AnalyticsService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin/analytics")
@Tag(name = "Analytics", description = "API for transaction analytics and reporting")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(summary = "Get transaction summary metrics for a date range")
    @GetMapping("/summary")
    @ResponseStatus(HttpStatus.OK)
    public TransactionSummaryDTO getSummary(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : end.minusDays(30);

        return analyticsService.getSummary(start, end);
    }

    @Operation(summary = "Get transaction trend data grouped by time period")
    @GetMapping("/trends")
    @ResponseStatus(HttpStatus.OK)
    public List<TransactionTrendPointDTO> getTrends(
            @RequestParam(defaultValue = "DAILY") String granularity,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : end.minusDays(30);

        return analyticsService.getTrends(granularity, start, end);
    }

    @Operation(summary = "Get top products by sales volume or revenue")
    @GetMapping("/top-products")
    @ResponseStatus(HttpStatus.OK)
    public List<TopProductDTO> getTopProducts(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "revenue") String sortBy) {

        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : end.minusDays(30);

        return analyticsService.getTopProducts(limit, start, end, sortBy);
    }
}
