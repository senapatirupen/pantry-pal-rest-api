package com.pantrypal.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InventoryItemRequest {
    @NotBlank(message = "Item name is required")
    @Size(min = 1, max = 100, message = "Item name must be between 1 and 100 characters")
    private String name;

    @NotBlank(message = "Category is required")
    @Pattern(regexp = "^(groceries|household|medicine|personal_care|other)$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Category must be one of: groceries, household, medicine, personal_care, other")
    private String category;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(in_stock|low|out_of_stock)$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Status must be one of: in_stock, low, out_of_stock")
    private String status;

    @NotBlank(message = "Frequency is required")
    @Pattern(regexp = "^(daily|weekly|monthly|occasional)$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Frequency must be one of: daily, weekly, monthly, occasional")
    private String frequency;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @DecimalMax(value = "100000.0", message = "Price cannot exceed 100,000")
    private BigDecimal price;

    @Size(max = 500, message = "Note cannot exceed 500 characters")
    private String note;

    @FutureOrPresent(message = "Need by date must be present or future")
    private LocalDate needBy;
}
