package com.pantrypal.controller;

import com.pantrypal.dto.request.*;
import com.pantrypal.dto.response.*;
import com.pantrypal.service.InventoryService;
import com.pantrypal.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory management endpoints")
public class InventoryController {

    private final InventoryService inventoryService;

//    @GetMapping
//    @Operation(summary = "Get all inventory items with optional filters")
//    public ResponseEntity<ApiResponse<PaginatedResponse<InventoryItemResponse>>> getItems(
//            @CurrentUser Long userId,
//            @RequestParam(required = false) String status,
//            @RequestParam(required = false) String category,
//            @RequestParam(required = false) String frequency,
//            @RequestParam(required = false) String search,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "20") int size,
//            @RequestParam(defaultValue = "updatedAt") String sortBy,
//            @RequestParam(defaultValue = "DESC") String sortDirection) {
//
//        PaginatedResponse<InventoryItemResponse> items = inventoryService.getItems(
//                userId, status, category, frequency, search,
//                page, size, sortBy, sortDirection);
//
//        return ResponseEntity.ok(ApiResponse.success(items));
//    }

//    @GetMapping
//    @Operation(summary = "Get all inventory items with optional filters")
//    public ResponseEntity<List<InventoryItemResponse>> getItems(
//            @CurrentUser Long userId,
//            @RequestParam(required = false) String status,
//            @RequestParam(required = false) String category,
//            @RequestParam(required = false) String frequency) {
//         List<InventoryItemResponse> items = inventoryService.getAllItemsByUserId(
//                userId);
//        return ResponseEntity.ok(items);
//    }

    @GetMapping("/all")
    @Operation(summary = "Get all inventory items with optional filters")
    public ResponseEntity<ApiResponse<List<InventoryItemResponse>>> getItems(
            @CurrentUser Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String frequency) {
        List<InventoryItemResponse> items = inventoryService.getAllItemsByUserId(
                userId);

        return ResponseEntity.ok(ApiResponse.success(items));
    }

//    @GetMapping("/{id}")
//    @Operation(summary = "Get a single inventory item by ID")
//    public ResponseEntity<ApiResponse<InventoryItemResponse>> getItem(
//            @CurrentUser Long userId,
//            @PathVariable Long id) {
//
//        InventoryItemResponse item = inventoryService.getItem(userId, id);
//        return ResponseEntity.ok(ApiResponse.success(item));
//    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single inventory item by ID")
    public ResponseEntity<InventoryItemResponse> getItem(
            @CurrentUser Long userId,
            @PathVariable Long id) {

        InventoryItemResponse item = inventoryService.getItem(userId, id);
        return ResponseEntity.ok(item);
    }

    @PostMapping
    @Operation(summary = "Create a new inventory item")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> createItem(
            @CurrentUser Long userId,
            @Valid @RequestBody InventoryItemRequest request) {

        InventoryItemResponse item = inventoryService.createItem(userId, request);
        return ResponseEntity.ok(ApiResponse.success(item, "Item created successfully"));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Create multiple inventory items at once")
    public ResponseEntity<ApiResponse<java.util.List<InventoryItemResponse>>> bulkCreateItems(
            @CurrentUser Long userId,
            @Valid @RequestBody BulkItemsRequest request) {

        java.util.List<InventoryItemResponse> items = inventoryService.bulkCreateItems(userId, request);
        return ResponseEntity.ok(ApiResponse.success(items, "Items created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an entire inventory item")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> updateItem(
            @CurrentUser Long userId,
            @PathVariable Long id,
            @Valid @RequestBody InventoryItemRequest request) {

        InventoryItemResponse item = inventoryService.updateItem(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(item, "Item updated successfully"));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Partially update an inventory item")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> patchItem(
            @CurrentUser Long userId,
            @PathVariable Long id,
            @Valid @RequestBody InventoryItemRequest request) {

        InventoryItemResponse item = inventoryService.patchItem(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(item, "Item updated successfully"));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update only the status of an item")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> updateItemStatus(
            @CurrentUser Long userId,
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {

        InventoryItemResponse item = inventoryService.updateItemStatus(userId, id, request);
        return ResponseEntity.ok(ApiResponse.success(item, "Status updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an inventory item")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @CurrentUser Long userId,
            @PathVariable Long id) {

        inventoryService.deleteItem(userId, id);
        return ResponseEntity.ok(ApiResponse.success(null, "Item deleted successfully"));
    }

    @DeleteMapping("/bulk")
    @Operation(summary = "Delete multiple inventory items")
    public ResponseEntity<ApiResponse<Integer>> bulkDeleteItems(
            @CurrentUser Long userId,
            @Valid @RequestBody BulkDeleteRequest request) {

        int deletedCount = inventoryService.bulkDeleteItems(userId, request.getIds());
        return ResponseEntity.ok(ApiResponse.success(deletedCount, "Items deleted successfully"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search inventory items by name")
    public ResponseEntity<ApiResponse<java.util.List<InventoryItemResponse>>> searchItems(
            @CurrentUser Long userId,
            @RequestParam String q) {

        java.util.List<InventoryItemResponse> items = inventoryService.searchItems(userId, q);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming items that need to be restocked")
    public ResponseEntity<ApiResponse<java.util.List<InventoryItemResponse>>> getUpcomingItems(
            @CurrentUser Long userId,
            @RequestParam(defaultValue = "7") int days) {

        java.util.List<InventoryItemResponse> items = inventoryService.getUpcomingItems(userId, days);
        return ResponseEntity.ok(ApiResponse.success(items));
    }
}
