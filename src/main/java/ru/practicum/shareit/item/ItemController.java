package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.group.Create;
import ru.practicum.shareit.item.dto.ItemDto;
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
    public ItemDto create(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @Validated(Create.class) @RequestBody ItemDto itemDto
    ) {
        log.info("Запрос на добавление вещи {} пользователю с id {}", itemDto, userId);
        return itemService.create(itemDto, userId);
    }

    @PatchMapping("{itemId}")
    public ItemDto patch(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody ItemDto itemDto
    ) {
        log.info("Запрос на изменение вещи с id {} {} пользователем с id {}", itemId, itemDto, userId);
        return itemService.update(userId, itemId, itemDto);
    }

    @GetMapping("{itemId}")
    public ItemDto getItem(@PathVariable long itemId) {
        log.info("Запрос на получение вещи с id {} ", itemId);
        return itemService.getItem(itemId);
    }

    @GetMapping
    public Collection<ItemDto> findAll(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info("Запрос на получение всех вещей пользователя с id {} ", userId);
        return itemService.getAllUserItems(userId);
    }

    @GetMapping("search")
    public Collection<ItemDto> search(@RequestParam(required = false) String text) {
        log.info("Запрос на получение списка вещей содержащих : {} ", text);
        if (text == null || text.isBlank()) {
            log.info("Возращаем пустой список");
            return Collections.emptyList();
        }
        return itemService.search(text);
    }
}
