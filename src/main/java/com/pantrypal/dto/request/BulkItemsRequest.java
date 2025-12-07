package com.pantrypal.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BulkItemsRequest {
    @NotEmpty(message = "Items list cannot be empty")
    @Size(max = 100, message = "Cannot create more than 100 items at once")
    private java.util.List<InventoryItemRequest> items;
}
