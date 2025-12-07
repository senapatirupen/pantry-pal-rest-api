package com.pantrypal.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InventoryItemResponse {
    private Long id;
    private String name;
    private String category;
    private String status;
    private String frequency;
    private Double price;
    private String note;
    private LocalDate needBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
