package com.pantrypal.service;

import com.pantrypal.dto.response.StatsResponse;
import com.pantrypal.dto.response.FrequencyReportResponse;
import com.pantrypal.entity.enums.Status;
import com.pantrypal.repository.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {

    private final InventoryItemRepository itemRepository;

    @Transactional(readOnly = true)
    public StatsResponse getStatsSummary(Long userId) {
        log.debug("Fetching stats summary for user: {}", userId);

        Long totalItems = itemRepository.countByUserId(userId);
        Long lowStockItems = itemRepository.countByUserIdAndStatus(userId, Status.LOW);
        Long outOfStockItems = itemRepository.countByUserIdAndStatus(userId, Status.OUT_OF_STOCK);
        BigDecimal averagePrice = itemRepository.averagePriceByUser(userId);
        BigDecimal totalSpending = itemRepository.totalSpendingByUser(userId);

        return StatsResponse.builder()
                .totalItems(totalItems)
                .lowStockItems(lowStockItems)
                .outOfStockItems(outOfStockItems)
                .averagePrice(averagePrice != null ? averagePrice.doubleValue() : 0.0)
                .totalSpending(totalSpending != null ? totalSpending.doubleValue() : 0.0)
                .build();
    }

    @Transactional(readOnly = true)
    public List<StatsResponse.MonthlySpending> getMonthlySpending(Long userId, int months) {
        log.debug("Fetching monthly spending for user: {} for {} months", userId, months);

        List<Object[]> results = itemRepository.findMonthlySpendingNative(userId, months);

        return results.stream()
                .map(row -> StatsResponse.MonthlySpending.builder()
                        .month((String) row[0])
                        .amount(((BigDecimal) row[1]).doubleValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StatsResponse.CategoryBreakdown> getCategoryBreakdown(Long userId) {
        log.debug("Fetching category breakdown for user: {}", userId);

        List<Object[]> results = itemRepository.findCategoryBreakdown(userId);

        return results.stream()
                .map(row -> StatsResponse.CategoryBreakdown.builder()
                        .category(((Enum<?>) row[0]).name().toLowerCase())
                        .count((Long) row[1])
                        .totalSpending(((BigDecimal) row[2]).doubleValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FrequencyReportResponse> getFrequencyReport(Long userId) {
        log.debug("Fetching frequency report for user: {}", userId);

        List<Object[]> results = itemRepository.findFrequencyReport(userId);

        return results.stream()
                .map(row -> FrequencyReportResponse.builder()
                        .frequency(((Enum<?>) row[0]).name().toLowerCase())
                        .count((Long) row[1])
                        .totalSpending(((BigDecimal) row[2]).doubleValue())
                        .build())
                .collect(Collectors.toList());
    }
}
