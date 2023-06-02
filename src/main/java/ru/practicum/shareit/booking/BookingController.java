package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.group.Create;

import java.util.Collection;

/**
 * TODO Sprint add-bookings.
 */
@Slf4j
@RestController
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDtoResponse create(@Validated(Create.class) @RequestBody BookingDtoRequest bookingDto,
                                     @RequestHeader("X-Sharer-User-Id") long userId
    ) {
        log.info("Запрос на добавление бронирования {} пользователем с id {}", bookingDto, userId);
        return bookingService.create(bookingDto,userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDtoResponse approve(@PathVariable long bookingId,
                                            @RequestParam Boolean approved,
                                            @RequestHeader("X-Sharer-User-Id") long userId
    ) {
        log.info("Запрос на подтвержение бронирования {} пользователем с id {} статус {}",bookingId, userId,approved);
        return bookingService.approve(bookingId,approved,userId);

    }

    @GetMapping("/{bookingId}")
    public BookingDtoResponse getBooking(
            @PathVariable long bookingId,
            @RequestHeader("X-Sharer-User-Id") long userId
    ) {
        log.info("Запрос на получение бронирования с id {} пользователем с id {}",bookingId, userId);
        return bookingService.getBooking(bookingId, userId);
    }

    @GetMapping
    public Collection<BookingDtoResponse> findAllByBookerId(
            @RequestParam(defaultValue = "ALL") BookingState state,
            @RequestHeader("X-Sharer-User-Id") long userId
    ) {
        log.info("Запрос на получение всех бронирований пользователя с id {} и состоянием {}",userId, state);
        return bookingService.findAllByBookerId(state,userId);
    }

    @GetMapping("/owner")
    public Collection<BookingDtoResponse> findAllByOwnerId(
            @RequestParam(defaultValue = "ALL") BookingState state,
            @RequestHeader("X-Sharer-User-Id") long userId
    ) {
        log.info("Запрос на получение всех бронирований у владельца с id {} и состоянием {}",userId, state);
        return bookingService.findAllByOwnerId(state,userId);
    }

}
