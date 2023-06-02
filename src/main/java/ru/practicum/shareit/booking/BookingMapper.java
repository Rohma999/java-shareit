package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {

    public static Booking toBooking(BookingDtoRequest bookingDtoRequest, User owner,Item item) {
        return Booking.builder()
                .start(bookingDtoRequest.getStart())
                .end(bookingDtoRequest.getEnd())
                .status(BookingStatus.WAITING)
                .booker(owner)
                .item(item)
                .build();
    }

    public static BookingDtoResponse toBookingDtoResponse(Booking booking) {
        return BookingDtoResponse.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(new BookingDtoResponse.ItemDtoShort(booking.getItem().getId(),booking.getItem().getName()))
                .booker(new BookingDtoResponse.UserDtoShort(booking.getBooker().getId()))
                .status(booking.getStatus())
                .build();
    }
}
