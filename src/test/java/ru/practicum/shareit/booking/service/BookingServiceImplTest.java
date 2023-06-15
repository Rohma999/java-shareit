package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;

import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplTest {
    private final BookingService bookingService;
    private final EntityManager entityManager;
    private final UserService userService;
    private final ItemService itemService;
    private BookingDtoResponse bookingDtoResponse;
    private ItemDtoResponse itemDto;
    private UserDto owner;

    @BeforeEach
    void initialize() {
        owner = userService.create(UserDto.builder().name("Misha").email("mi@mail.com").build());
        UserDto booker = userService.create(UserDto.builder().name("Vanya").email("va@mail.com").build());
        itemDto = itemService.create(ItemDtoRequest.builder().name("pen").description("blue pen")
                .available(true).build(), owner.getId());

        BookingDtoRequest bookingDtoRequest = BookingDtoRequest.builder()
                .start(now())
                .end(now().plusHours(2))
                .itemId(itemDto.getId())
                .build();

        bookingDtoResponse = bookingService.create(bookingDtoRequest, booker.getId());
        bookingService.approve(bookingDtoResponse.getId(), true, owner.getId());
    }

    @Test
    void saveTest() {
        Booking booking = entityManager
                .createQuery(
                        "SELECT booking " +
                                "FROM Booking booking",
                        Booking.class)
                .getSingleResult();
        assertThat(booking.getId(), notNullValue());
        assertThat(booking.getBooker().getId(),
                equalTo(bookingDtoResponse.getBooker().getId()));
        assertThat(booking.getItem().getId(),
                equalTo(bookingDtoResponse.getItem().getId()));
    }

    @Test
    void getAllBookingsTest() {
        List<BookingDtoResponse> approved = bookingService.findAllByBookerId(BookingState.ALL,
                bookingDtoResponse.getBooker().getId(), 0, 10);
        List<Booking> booking = entityManager.createQuery(
                        "SELECT booking " +
                                "FROM Booking booking " +
                                "WHERE booking.booker.id = :id",
                        Booking.class)
                .setParameter("id", bookingDtoResponse.getBooker().getId())
                .getResultList();
        assertThat(approved.get(0).getId(),
                equalTo(booking.get(0).getId()));
        assertThat(approved.size(),
                equalTo(booking.size()));
    }

    @Test
    void getBookingsByOwnerIdStatusTest() {
        List<BookingDtoResponse> bookings = bookingService.findAllByOwnerId(
                BookingState.ALL, owner.getId(), 0, 10);
        List<Booking> approvedBookings = entityManager.createQuery(
                        "SELECT booking " +
                                "FROM Booking booking " +
                                "JOIN booking.item item " +
                                "WHERE item.owner.id = :id AND booking.status = :status",
                        Booking.class)
                .setParameter("id", owner.getId())
                .setParameter("status", BookingStatus.APPROVED)
                .getResultList();
        assertThat(bookings.size(),
                equalTo(approvedBookings.size()));
        assertThat(bookings.size(),
                equalTo(1));
    }

    @Test
    void getAllBookingsEmptyListTest() {
        List<BookingDtoResponse> allBookings = bookingService.findAllByBookerId(
                BookingState.REJECTED, bookingDtoResponse.getBooker().getId(), 0, 10);
        List<Booking> approved = entityManager.createQuery(
                        "SELECT booking " +
                                "FROM Booking booking " +
                                "WHERE booking.booker.id = :id AND booking.status = :status",
                        Booking.class)
                .setParameter("id", bookingDtoResponse.getBooker().getId())
                .setParameter("status", BookingStatus.REJECTED)
                .getResultList();
        assertThat(allBookings.size(),
                equalTo(approved.size()));
        assertThat(allBookings.size(),
                equalTo(0));
    }

    @Test
    void getBookingsByOwnerIdTest() {
        List<BookingDtoResponse> bookings = bookingService.findAllByOwnerId(BookingState.ALL, owner.getId(), 0, 10);
        List<Booking> booking = entityManager.createQuery(
                        "SELECT booking " +
                                "FROM Booking booking " +
                                "JOIN booking.item item " +
                                "WHERE item.owner.id = :id",
                        Booking.class)
                .setParameter("id", owner.getId())
                .getResultList();
        assertThat(bookings.get(0).getId(),
                equalTo(booking.get(0).getId()));
        assertThat(bookings.size(),
                equalTo(booking.size()));
    }

    @Test
    void getBookingByIdTest() {
        BookingDtoResponse approved = bookingService.getBooking(bookingDtoResponse.getId(),
                bookingDtoResponse.getBooker().getId());
        Booking booking = entityManager
                .createQuery(
                        "SELECT booking " +
                                "FROM Booking booking " +
                                "WHERE booking.id = :id AND booking.booker.id = :bookerId",
                        Booking.class)
                .setParameter("bookerId", bookingDtoResponse.getBooker().getId())
                .setParameter("id", bookingDtoResponse.getId())
                .getSingleResult();
        assertThat(approved.getItem().getId(),
                equalTo(booking.getItem().getId()));
        assertThat(approved.getStart(),
                equalTo(booking.getStart()));
        assertThat(approved.getId(),
                equalTo(booking.getId()));
    }
}
