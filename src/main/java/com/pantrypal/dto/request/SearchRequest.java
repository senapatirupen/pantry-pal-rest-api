package com.pantrypal.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class SearchRequest {
    @NotBlank(message = "Search query is required")
    @Size(min = 1, max = 100, message = "Search query must be between 1 and 100 characters")
    private String q;

    private String category;
    private String status;
    private String frequency;
}
