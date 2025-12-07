package com.pantrypal.dto.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StatusUpdateRequest {
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(in_stock|low|out_of_stock)$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Status must be one of: in_stock, low, out_of_stock")
    private String status;
}
