package ru.practicum.shareit.request.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestItemRequestDto;
import ru.practicum.shareit.request.dto.ResponseItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@Slf4j
@RestController
@Validated
@RequestMapping(path = "/requests")
public class ItemRequestController {

    public static final String USER_ID = "X-Sharer-User-Id";

    private final ItemRequestService itemRequestService;

    @Autowired
    public ItemRequestController(ItemRequestService itemRequestService) {
        this.itemRequestService = itemRequestService;
    }

    @PostMapping
    public ResponseItemRequestDto create(
            @Valid @RequestBody RequestItemRequestDto requestItemRequestDto,
            @RequestHeader(USER_ID) @Min(1) long userId
    ) {
        log.info("Запрос на добавление запроса вещи {} пользователем с id {}", requestItemRequestDto, userId);
        return itemRequestService.create(requestItemRequestDto, userId);
    }

    @GetMapping
    public List<ResponseItemRequestDto> getAllByRequesterId(@RequestHeader(USER_ID) @Min(1) long userId) {
        log.info("Запрос на получение всех запросов вещей пользователя с id {}", userId);
        return itemRequestService.findAllByUserId(userId);
    }

    @GetMapping("/all")
    public List<ResponseItemRequestDto> getAll(
            @RequestHeader(USER_ID) @Min(1) long userId,
            @RequestParam(name = "from", defaultValue = "0") @Min(0) int from,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) int size) {
        log.info("Запрос на получение всех запросов вещей пользователем с id {},c {} элемента, количество {}",
                userId, from, size);
        return itemRequestService.findAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseItemRequestDto getRequest(
            @RequestHeader(USER_ID) @Min(1) long userId,
            @PathVariable @Min(1) long requestId
    ) {
        log.info("Запрос на получение запроса вещи с id{} , пользователем с id {}",
                requestId, userId);
        return itemRequestService.getById(requestId, userId);
    }
}
