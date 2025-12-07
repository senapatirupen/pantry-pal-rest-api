package com.pantrypal.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatsResponse {
    private Long totalItems;
    private Long lowStockItems;
    private Long outOfStockItems;
    private Double averagePrice;
    private Double totalSpending;
    private List<MonthlySpending> monthlySpending;
    private List<CategoryBreakdown> categoryBreakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlySpending {
        private String month;
        private Double amount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryBreakdown {
        private String category;
        private Long count;
        private Double totalSpending;
    }
}