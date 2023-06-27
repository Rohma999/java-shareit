package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@RestController
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    private static final String USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> createItem(@Valid @RequestBody ItemDto itemDto, @RequestHeader(USER_ID) long userId) {
        log.info("POST /items, userId={}", userId);
        return itemClient.createItem(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestBody ItemDto itemDto, @PathVariable long itemId,
                                             @RequestHeader(USER_ID) long userId) {
        log.info("PATCH /items/{}, userId={}", itemId, userId);
        return itemClient.updateItem(itemDto, itemId, userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@PathVariable long itemId, @RequestHeader(USER_ID) long userId) {
        log.info("GET /items/{}, userId={}", itemId, userId);
        return itemClient.getById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByOwnerId(@RequestHeader(USER_ID) long userId,
                                                  @RequestParam(name = "from", defaultValue = "0") @Min(0) int from,
                                                  @RequestParam(name = "size", defaultValue = "10") @Min(1) int size) {
        log.info("GET /items, userId={}, from={}, size={}", userId, from, size);
        return itemClient.findAllItemsByOwnerId(userId, from, size);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchByText(@RequestParam(defaultValue = "") String text,
                                               @RequestParam(name = "from", defaultValue = "0") @Min(0) int from,
                                               @RequestParam(name = "size", defaultValue = "10") @Min(1) int size) {
        log.info("Получен запрос GET /items/search?text={}, from={}, size={}", text, from, size);
        return itemClient.searchByText(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@Valid @RequestBody CommentDto commentDto, @PathVariable long itemId,
                                             @RequestHeader(USER_ID) long userId) {
        log.info("Получен запрос к эндпоинту POST /items/{}/comment, userId={}", itemId, userId);
        return itemClient.addComment(commentDto, userId, itemId);
    }

}