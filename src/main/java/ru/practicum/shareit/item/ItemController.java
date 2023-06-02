package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.group.Create;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.Collections;

@Slf4j
@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDtoResponse create(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @Validated(Create.class) @RequestBody ItemDtoRequest itemDto
    ) {
        log.info("Запрос на добавление вещи {} пользователю с id {}", itemDto, userId);
        return itemService.create(itemDto, userId);
    }

    @PostMapping("{itemId}/comment")
    public CommentDtoResponse addComment(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable long itemId,
            @Validated(Create.class) @RequestBody CommentDtoRequest commentDtoRequest
    ) {
        log.info("Запрос на добавление отзыва {} для вещи с id {}  пользователем с id {}", commentDtoRequest, itemId, userId);
        return itemService.addComment(userId, itemId, commentDtoRequest);
    }

    @PatchMapping("{itemId}")
    public ItemDtoResponse patch(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody ItemDtoRequest itemDtoRequest
    ) {
        log.info("Запрос на изменение вещи с id {} {} пользователем с id {}", itemId, itemDtoRequest, userId);
        return itemService.update(userId, itemId, itemDtoRequest);
    }

    @GetMapping("{itemId}")
    public ItemDtoResponse getItem(@PathVariable long itemId,@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Запрос на получение вещи с id {} пользователем с id {} ", itemId,userId);
        return itemService.getItem(itemId,userId);
    }

    @GetMapping
    public Collection<ItemDtoResponse> findAll(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Запрос на получение всех вещей пользователя с id {} ", userId);
        return itemService.getAllUserItems(userId);
    }

    @GetMapping("search")
    public Collection<ItemDtoResponse> search(@RequestParam(required = false) String text) {
        log.info("Запрос на получение списка вещей содержащих : {} ", text);
        if (text == null || text.isBlank()) {
            log.info("Возращаем пустой список");
            return Collections.emptyList();
        }
        return itemService.search(text);
    }
}
