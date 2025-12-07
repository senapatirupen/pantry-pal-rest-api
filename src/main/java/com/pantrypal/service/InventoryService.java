package com.pantrypal.service;

import com.pantrypal.dto.request.*;
import com.pantrypal.dto.response.InventoryItemResponse;
import com.pantrypal.dto.response.PaginatedResponse;
import com.pantrypal.entity.InventoryItem;
import com.pantrypal.entity.User;
import com.pantrypal.entity.enums.Category;
import com.pantrypal.entity.enums.Status;
import com.pantrypal.entity.enums.Frequency;
import com.pantrypal.exception.*;
import com.pantrypal.repository.InventoryItemRepository;
import com.pantrypal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryItemRepository itemRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PaginatedResponse<InventoryItemResponse> getItems(
            Long userId,
            String status,
            String category,
            String frequency,
            String search,
            int page,
            int size,
            String sortBy,
            String sortDirection) {

        log.debug("Fetching items for user: {}, page: {}, size: {}", userId, page, size);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(sortDirection), sortBy)
        );

        Page<InventoryItem> itemsPage;

        if (search != null && !search.trim().isEmpty()) {
            // Search implementation
            itemsPage = itemRepository.findByUserIdAndNameContainingIgnoreCase(
                    userId, search, pageable);
        } else {
            itemsPage = itemRepository.findByUserId(userId, pageable);
        }

        List<InventoryItemResponse> items = itemsPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PaginatedResponse.<InventoryItemResponse>builder()
                .content(items)
                .page(itemsPage.getNumber())
                .size(itemsPage.getSize())
                .totalElements(itemsPage.getTotalElements())
                .totalPages(itemsPage.getTotalPages())
                .last(itemsPage.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getAllItemsByUserId(
            Long userId) {
        log.debug("Fetching all items for user: {}", userId);
        List<InventoryItem> itemsPage
              = itemRepository.findByUserId(userId);
        List<InventoryItemResponse> items = itemsPage.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return items;
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getItemsWithFilters(
            Long userId,
            String status,
            String category,
            String frequency) {

        log.debug("Fetching filtered items for user: {}", userId);

        List<InventoryItem> items;

        if (status != null && category != null && frequency != null) {
            items = itemRepository.findByUserIdAndCategoryAndStatusAndFrequency(
                    userId,
                    Category.valueOf(category.toUpperCase()),
                    Status.valueOf(status.toUpperCase()),
                    Frequency.valueOf(frequency.toUpperCase())
            );
        } else if (status != null && category != null) {
            items = itemRepository.findByUserIdAndCategoryAndStatus(
                    userId,
                    Category.valueOf(category.toUpperCase()),
                    Status.valueOf(status.toUpperCase())
            );
        } else if (status != null) {
            items = itemRepository.findByUserIdAndStatus(
                    userId,
                    Status.valueOf(status.toUpperCase())
            );
        } else if (category != null) {
            items = itemRepository.findByUserIdAndCategory(
                    userId,
                    Category.valueOf(category.toUpperCase())
            );
        } else if (frequency != null) {
            items = itemRepository.findByUserIdAndFrequency(
                    userId,
                    Frequency.valueOf(frequency.toUpperCase())
            );
        } else {
            items = itemRepository.findByUserId(userId);
        }

        return items.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InventoryItemResponse getItem(Long userId, Long itemId) {
        log.debug("Fetching item: {} for user: {}", itemId, userId);

        InventoryItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        validateUserOwnership(userId, item);

        return mapToResponse(item);
    }

    @Transactional
    public InventoryItemResponse createItem(Long userId, InventoryItemRequest request) {
        log.info("Creating item for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        InventoryItem item = InventoryItem.builder()
                .user(user)
                .name(request.getName())
                .category(Category.valueOf(request.getCategory().toUpperCase()))
                .status(Status.valueOf(request.getStatus().toUpperCase()))
                .frequency(Frequency.valueOf(request.getFrequency().toUpperCase()))
                .price(request.getPrice() != null ? request.getPrice() : null)
                .note(request.getNote())
                .needBy(request.getNeedBy())
                .build();

        item = itemRepository.save(item);
        log.info("Item created: {}", item.getId());

        return mapToResponse(item);
    }

    @Transactional
    public List<InventoryItemResponse> bulkCreateItems(Long userId, BulkItemsRequest request) {
        log.info("Bulk creating {} items for user: {}", request.getItems().size(), userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<InventoryItem> items = request.getItems().stream()
                .map(req -> InventoryItem.builder()
                        .user(user)
                        .name(req.getName())
                        .category(Category.valueOf(req.getCategory().toUpperCase()))
                        .status(Status.valueOf(req.getStatus().toUpperCase()))
                        .frequency(Frequency.valueOf(req.getFrequency().toUpperCase()))
                        .price(req.getPrice() != null ? req.getPrice() : null)
                        .note(req.getNote())
                        .needBy(req.getNeedBy())
                        .build())
                .collect(Collectors.toList());

        items = itemRepository.saveAll(items);
        log.info("Bulk created {} items", items.size());

        return items.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public InventoryItemResponse updateItem(Long userId, Long itemId, InventoryItemRequest request) {
        log.info("Updating item: {} for user: {}", itemId, userId);

        InventoryItem item = getItemEntity(userId, itemId);

        item.setName(request.getName());
        item.setCategory(Category.valueOf(request.getCategory().toUpperCase()));
        item.setStatus(Status.valueOf(request.getStatus().toUpperCase()));
        item.setFrequency(Frequency.valueOf(request.getFrequency().toUpperCase()));
        item.setPrice(request.getPrice() != null ? request.getPrice() : null);
        item.setNote(request.getNote());
        item.setNeedBy(request.getNeedBy());

        item = itemRepository.save(item);
        log.info("Item updated: {}", itemId);

        return mapToResponse(item);
    }

    @Transactional
    public InventoryItemResponse patchItem(Long userId, Long itemId, InventoryItemRequest request) {
        log.info("Patching item: {} for user: {}", itemId, userId);

        InventoryItem item = getItemEntity(userId, itemId);

        if (request.getName() != null) item.setName(request.getName());
        if (request.getCategory() != null) item.setCategory(Category.valueOf(request.getCategory().toUpperCase()));
        if (request.getStatus() != null) item.setStatus(Status.valueOf(request.getStatus().toUpperCase()));
        if (request.getFrequency() != null) item.setFrequency(Frequency.valueOf(request.getFrequency().toUpperCase()));
        if (request.getPrice() != null) item.setPrice(request.getPrice());
        if (request.getNote() != null) item.setNote(request.getNote());
        if (request.getNeedBy() != null) item.setNeedBy(request.getNeedBy());

        item = itemRepository.save(item);
        log.info("Item patched: {}", itemId);

        return mapToResponse(item);
    }

    @Transactional
    public InventoryItemResponse updateItemStatus(Long userId, Long itemId, StatusUpdateRequest request) {
        log.info("Updating status for item: {} to {}", itemId, request.getStatus());

        InventoryItem item = getItemEntity(userId, itemId);
        item.setStatus(Status.valueOf(request.getStatus().toUpperCase()));

        item = itemRepository.save(item);
        log.info("Item status updated: {}", itemId);

        return mapToResponse(item);
    }

    @Transactional
    public void deleteItem(Long userId, Long itemId) {
        log.info("Deleting item: {} for user: {}", itemId, userId);

        InventoryItem item = getItemEntity(userId, itemId);
        itemRepository.delete(item);

        log.info("Item deleted: {}", itemId);
    }

    @Transactional
    public int bulkDeleteItems(Long userId, List<Long> itemIds) {
        log.info("Bulk deleting {} items for user: {}", itemIds.size(), userId);

        // Verify all items belong to the user
        Long count = itemRepository.countByUserIdAndIdIn(userId, itemIds);
        if (count != itemIds.size()) {
            throw new UnauthorizedException("Some items do not belong to the user");
        }

        itemRepository.deleteByUserIdAndIdIn(userId, itemIds);
        log.info("Bulk deleted {} items", itemIds.size());

        return itemIds.size();
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> searchItems(Long userId, String query) {
        log.debug("Searching items for user: {} with query: {}", userId, query);

        List<InventoryItem> items = itemRepository.searchByUserIdAndName(userId, query);

        return items.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getUpcomingItems(Long userId, int days) {
        log.debug("Fetching upcoming items for user: {} within {} days", userId, days);

        // Implementation would use date calculation
        // For simplicity, returning all items
        List<InventoryItem> items = itemRepository.findByUserId(userId);

        return items.stream()
                .map(this::mapToResponse)
                .limit(10) // Limit for upcoming items
                .collect(Collectors.toList());
    }

    private InventoryItem getItemEntity(Long userId, Long itemId) {
        InventoryItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + itemId));

        validateUserOwnership(userId, item);
        return item;
    }

    private void validateUserOwnership(Long userId, InventoryItem item) {
        if (!item.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You don't have permission to access this item");
        }
    }

    private InventoryItemResponse mapToResponse(InventoryItem item) {
        return InventoryItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .category(item.getCategory().name().toLowerCase())
                .status(item.getStatus().name().toLowerCase())
                .frequency(item.getFrequency().name().toLowerCase())
                .price(item.getPrice() != null ? item.getPrice().doubleValue() : null)
                .note(item.getNote())
                .needBy(item.getNeedBy())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
