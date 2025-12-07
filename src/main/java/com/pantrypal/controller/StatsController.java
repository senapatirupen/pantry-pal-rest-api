package com.pantrypal.controller;

import com.pantrypal.dto.response.*;
import com.pantrypal.service.StatsService;
import com.pantrypal.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Statistics and analytics endpoints")
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/summary")
    @Operation(summary = "Get summary statistics")
    public ResponseEntity<ApiResponse<StatsResponse>> getStatsSummary(@CurrentUser Long userId) {
        StatsResponse stats = statsService.getStatsSummary(userId);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/monthly-spending")
    @Operation(summary = "Get monthly spending data")
    public ResponseEntity<ApiResponse<java.util.List<StatsResponse.MonthlySpending>>> getMonthlySpending(
            @CurrentUser Long userId,
            @RequestParam(defaultValue = "12") int months) {

        java.util.List<StatsResponse.MonthlySpending> spending = statsService.getMonthlySpending(userId, months);
        return ResponseEntity.ok(ApiResponse.success(spending));
    }

    @GetMapping("/category-breakdown")
    @Operation(summary = "Get category breakdown statistics")
    public ResponseEntity<ApiResponse<java.util.List<StatsResponse.CategoryBreakdown>>> getCategoryBreakdown(
            @CurrentUser Long userId) {

        java.util.List<StatsResponse.CategoryBreakdown> breakdown = statsService.getCategoryBreakdown(userId);
        return ResponseEntity.ok(ApiResponse.success(breakdown));
    }

    @GetMapping("/frequency-report")
    @Operation(summary = "Get frequency report")
    public ResponseEntity<ApiResponse<java.util.List<FrequencyReportResponse>>> getFrequencyReport(
            @CurrentUser Long userId) {

        java.util.List<FrequencyReportResponse> report = statsService.getFrequencyReport(userId);
        return ResponseEntity.ok(ApiResponse.success(report));
    }
}
