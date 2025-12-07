package com.pantrypal.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class BulkDeleteRequest {
    @NotEmpty(message = "Item IDs cannot be empty")
    private java.util.List<Long> ids;
}
