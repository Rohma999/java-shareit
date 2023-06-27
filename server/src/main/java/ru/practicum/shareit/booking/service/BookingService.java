package ru.practicum.shareit.booking.service;


import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {

    BookingDtoResponse create(BookingDtoRequest bookingDtoRequest, long userId);

    BookingDtoResponse approve(long bookingId, Boolean approved, long userId);

    BookingDtoResponse getBooking(long bookingId, long userId);

    List<BookingDtoResponse> findAllByBookerId(BookingState bookingState, long userId,int from,int size);

    List<BookingDtoResponse> findAllByOwnerId(BookingState bookingState, long userId,int from,int size);
}
