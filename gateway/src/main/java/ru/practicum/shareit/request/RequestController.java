package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestController {

    private final RequestClient requestClient;

    private static final String USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> createItemRequest(@Valid @RequestBody RequestDto requestDto,
                                                    @RequestHeader(USER_ID) @Min(1) long userId) {
        log.info("Получен запрос POST /requests");
        return requestClient.createItemRequest(requestDto, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemRequestByRequester(@RequestHeader(USER_ID) @Min(1) long userId) {
        log.info("Получен запрос GET /requests");
        return requestClient.findAllByRequesterId(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItemRequest(@RequestHeader(USER_ID) @Min(1) long userId,
                                                    @RequestParam(name = "from", defaultValue = "0") @Min(0) int from,
                                                    @RequestParam(name = "size", defaultValue = "10") @Min(1) int size) {
        log.info("Получен запрос GET /requests/all?from={from}&size={size}");
        return requestClient.findAll(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequest(@RequestHeader(USER_ID) @Min(1) long userId,
                                                 @PathVariable @Min(1) long requestId) {
        log.info("Получен запрос GET /requests/{requestId}");
        return requestClient.getById(requestId, userId);
    }

}