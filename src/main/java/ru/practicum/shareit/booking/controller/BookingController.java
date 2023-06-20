package ru.practicum.shareit.booking.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.group.Create;

import javax.validation.constraints.Min;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@Validated
public class BookingController {

    public static final String USER_ID = "X-Sharer-User-Id";

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDtoResponse create(@Validated(Create.class) @RequestBody BookingDtoRequest bookingDto,
                                     @RequestHeader(USER_ID) long userId
    ) {
        log.info("Запрос на добавление бронирования {} пользователем с id {}", bookingDto, userId);
        return bookingService.create(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoResponse approve(@PathVariable long bookingId,
                                      @RequestParam Boolean approved,
                                      @RequestHeader(USER_ID) long userId
    ) {
        log.info("Запрос на подтвержение бронирования {} пользователем с id {} статус {}", bookingId, userId, approved);
        return bookingService.approve(bookingId, approved, userId);

    }

    @GetMapping("/{bookingId}")
    public BookingDtoResponse getBooking(
            @PathVariable long bookingId,
            @RequestHeader(USER_ID) long userId
    ) {
        log.info("Запрос на получение бронирования с id {} пользователем с id {}", bookingId, userId);
        return bookingService.getBooking(bookingId, userId);
    }

    @GetMapping
    public List<BookingDtoResponse> findAllByBookerId(
            @RequestParam(defaultValue = "ALL") BookingState state,
            @RequestHeader(USER_ID) long userId,
            @RequestParam(name = "from", defaultValue = "0") @Min(0) int from,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) int size
    ) {
        log.info("Запрос на получение всех бронирований пользователя с id {} и состоянием {} ,с {},количество {}",
                userId, state,from,size);
        return bookingService.findAllByBookerId(state, userId,from,size);
    }

    @GetMapping("/owner")
    public List<BookingDtoResponse> findAllByOwnerId(
            @RequestParam(defaultValue = "ALL") BookingState state,
            @RequestHeader(USER_ID) long userId,
            @RequestParam(name = "from", defaultValue = "0") @Min(0) int from,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) int size
    ) {
        log.info("Запрос на получение всех бронирований у владельца с id {} и состоянием {},с {},количество {}",
                userId, state,from,size);
        return bookingService.findAllByOwnerId(state, userId,from,size);
    }

}
