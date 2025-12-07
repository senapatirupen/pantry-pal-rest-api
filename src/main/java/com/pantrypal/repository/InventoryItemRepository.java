package com.pantrypal.repository;

import com.pantrypal.entity.InventoryItem;
import com.pantrypal.entity.enums.Category;
import com.pantrypal.entity.enums.Status;
import com.pantrypal.entity.enums.Frequency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    // Find items by user with pagination
    Page<InventoryItem> findByUserId(Long userId, Pageable pageable);
    List<InventoryItem> findByUserId(Long userId);
    // Find items by user and filters
    List<InventoryItem> findByUserIdAndCategory(Long userId, Category category);
    List<InventoryItem> findByUserIdAndStatus(Long userId, Status status);
    List<InventoryItem> findByUserIdAndFrequency(Long userId, Frequency frequency);
    List<InventoryItem> findByUserIdAndCategoryAndStatus(Long userId, Category category, Status status);

    // Search by name
    @Query("SELECT i FROM InventoryItem i WHERE i.user.id = :userId AND " +
            "LOWER(i.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<InventoryItem> searchByUserIdAndName(@Param("userId") Long userId, @Param("query") String query);

    // Upcoming items
    @Query("SELECT i FROM InventoryItem i WHERE i.user.id = :userId " +
            "AND i.needBy IS NOT NULL AND i.needBy <= :date " +
            "ORDER BY i.needBy ASC")
    List<InventoryItem> findUpcomingItems(@Param("userId") Long userId, @Param("date") LocalDate date);

    // Statistics queries
    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.user.id = :userId AND i.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Status status);

    @Query("SELECT COALESCE(SUM(i.price), 0) FROM InventoryItem i WHERE i.user.id = :userId AND i.price IS NOT NULL")
    BigDecimal totalSpendingByUser(@Param("userId") Long userId);

    @Query("SELECT COALESCE(AVG(i.price), 0) FROM InventoryItem i WHERE i.user.id = :userId AND i.price IS NOT NULL")
    BigDecimal averagePriceByUser(@Param("userId") Long userId);

    // Monthly spending (MySQL specific)
    @Query(value = """
        SELECT 
            DATE_FORMAT(i.created_at, '%Y-%m') as month,
            COALESCE(SUM(i.price), 0) as amount
        FROM inventory_items i
        WHERE i.user_id = :userId
            AND i.price IS NOT NULL
            AND i.created_at >= DATE_SUB(CURRENT_DATE, INTERVAL :months MONTH)
        GROUP BY DATE_FORMAT(i.created_at, '%Y-%m')
        ORDER BY month DESC
        """, nativeQuery = true)
    List<Object[]> findMonthlySpendingNative(@Param("userId") Long userId, @Param("months") int months);

    // Category breakdown
    @Query("""
        SELECT i.category as category, 
               COUNT(i) as count, 
               COALESCE(SUM(i.price), 0) as totalSpending
        FROM InventoryItem i
        WHERE i.user.id = :userId
        GROUP BY i.category
        ORDER BY COUNT(i) DESC
        """)
    List<Object[]> findCategoryBreakdown(@Param("userId") Long userId);

    // Frequency report
    @Query("""
        SELECT i.frequency as frequency, 
               COUNT(i) as count, 
               COALESCE(SUM(i.price), 0) as totalSpending
        FROM InventoryItem i
        WHERE i.user.id = :userId
        GROUP BY i.frequency
        ORDER BY 
            CASE i.frequency
                WHEN 'DAILY' THEN 1
                WHEN 'WEEKLY' THEN 2
                WHEN 'MONTHLY' THEN 3
                WHEN 'OCCASIONAL' THEN 4
            END
        """)
    List<Object[]> findFrequencyReport(@Param("userId") Long userId);

    // Bulk operations
    void deleteByUserIdAndIdIn(Long userId, List<Long> ids);

    boolean existsByUserIdAndId(Long userId, Long id);

    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.user.id = :userId AND i.id IN :ids")
    Long countByUserIdAndIdIn(@Param("userId") Long userId, @Param("ids") List<Long> ids);

    Page<InventoryItem> findByUserIdAndNameContainingIgnoreCase(Long userId, String search, Pageable pageable);

    List<InventoryItem> findByUserIdAndCategoryAndStatusAndFrequency(Long userId, Category valueOf, Status valueOf1, Frequency valueOf2);
}
