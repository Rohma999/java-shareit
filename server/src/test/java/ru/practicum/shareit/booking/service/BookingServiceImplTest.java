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

import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceImplTest {
    private final BookingService bookingService;
    private final EntityManager entityManager;
    private final UserService userService;
    private final ItemService itemService;
    private BookingDtoResponse bookingDtoResponse;

    private BookingDtoRequest bookingDtoRequest;
    private ItemDtoResponse itemDto;
    private UserDto owner;
    private UserDto booker;

    @BeforeEach
    void initialize() {
        owner = userService.create(UserDto.builder().name("Misha").email("mi@mail.com").build());
        booker = userService.create(UserDto.builder().name("Vanya").email("va@mail.com").build());
        itemDto = itemService.create(ItemDtoRequest.builder().name("pen").description("blue pen")
                .available(true).build(), owner.getId());

        bookingDtoRequest = BookingDtoRequest.builder()
                .start(now().plusHours(1))
                .end(now().plusHours(2))
                .itemId(itemDto.getId())
                .build();

        bookingDtoResponse = bookingService.create(bookingDtoRequest, booker.getId());
        bookingService.approve(bookingDtoResponse.getId(), true, owner.getId());
    }

    private void createBookingDto(BookingStatus bookingStatus) {
        bookingDtoRequest = BookingDtoRequest.builder()
                .start(now().minusHours(2))
                .end(now().minusHours(1))
                .itemId(itemDto.getId())
                .build();

        bookingDtoResponse = bookingService.create(bookingDtoRequest, booker.getId());
        if (bookingStatus == BookingStatus.APPROVED) {
            bookingService.approve(bookingDtoResponse.getId(), true, owner.getId());
        }
        if (bookingStatus == BookingStatus.REJECTED) {
            bookingService.approve(bookingDtoResponse.getId(), false, owner.getId());
        }
    }

    @Test
    void shouldNotCreateBookingWithNotFoundItemId() {
        bookingDtoRequest = BookingDtoRequest.builder()
                .start(now().plusHours(1))
                .end(now().plusHours(2))
                .itemId(500L)
                .build();
        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.create(bookingDtoRequest, booker.getId())
        );
        assertEquals("Вещь с id 500 не существует",
                exception.getMessage());
    }

    @Test
    void shouldNotCreateBookingWithNotFoundUserId() {
        bookingDtoRequest = BookingDtoRequest.builder()
                .start(now().plusHours(1))
                .end(now().plusHours(2))
                .itemId(itemDto.getId())
                .build();
        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.create(bookingDtoRequest, 500)
        );
        assertEquals("Пользователь с id 500 не существует",
                exception.getMessage());
    }

    @Test
    void shouldNotCreateBookingWithWrongTime() {
        bookingDtoRequest = BookingDtoRequest.builder()
                .start(now().plusHours(2))
                .end(now().plusHours(1))
                .itemId(itemDto.getId())
                .build();
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.create(bookingDtoRequest, booker.getId())
        );
        assertEquals("Дата начала бронирования должна быть раньше даты возврата",
                exception.getMessage());
    }

    @Test
    void shouldReturnAllBookingsByBookerIdTest() {
        createBookingDto(BookingStatus.REJECTED);
        List<BookingDtoResponse> bookings = bookingService.findAllByBookerId(BookingState.ALL,
                bookingDtoResponse.getBooker().getId(), 0, 10);
        List<Booking> checkBookings = entityManager.createQuery(
                        "SELECT booking " +
                                "FROM Booking booking " +
                                "WHERE booking.booker.id = :id",
                        Booking.class)
                .setParameter("id", bookingDtoResponse.getBooker().getId())
                .getResultList();
        assertThat(bookings.get(0).getId(),
                equalTo(checkBookings.get(0).getId()));
        assertThat(bookings.size(),
                equalTo(checkBookings.size()));
        assertThat(bookings.size(),
                equalTo(2));
    }

    @Test
    void shouldReturnRejectBookingsByBookerIdTest() {
        createBookingDto(BookingStatus.REJECTED);
        List<BookingDtoResponse> bookings = bookingService.findAllByBookerId(BookingState.REJECTED,
                bookingDtoResponse.getBooker().getId(), 0, 10);
        List<Booking> checkBookings = entityManager.createQuery(
                        "SELECT booking " +
                                "FROM Booking booking " +
                                "WHERE booking.booker.id = :id AND booking.status = :status",
                        Booking.class)
                .setParameter("id", bookingDtoResponse.getBooker().getId())
                .setParameter("status", BookingStatus.REJECTED)
                .getResultList();
        assertThat(bookings.get(0).getId(),
                equalTo(checkBookings.get(0).getId()));
        assertThat(bookings.size(),
                equalTo(checkBookings.size()));
        assertThat(bookings.size(),
                equalTo(1));
    }

    @Test
    void shouldReturnFutureBookingsByBookerIdTest() {
        List<BookingDtoResponse> bookings = bookingService.findAllByBookerId(BookingState.FUTURE,
                bookingDtoResponse.getBooker().getId(), 0, 10);
        List<Booking> checkBookings = entityManager.createQuery(
                        "SELECT booking " +
                                "FROM Booking booking " +
                                "WHERE booking.booker.id = :id AND booking.start > :now",
                        Booking.class)
                .setParameter("id", bookingDtoResponse.getBooker().getId())
                .setParameter("now", LocalDateTime.now())
                .getResultList();
        assertThat(bookings.get(0).getId(),
                equalTo(checkBookings.get(0).getId()));
        assertThat(bookings.size(),
                equalTo(checkBookings.size()));
        assertThat(bookings.size(),
                equalTo(1));
    }

    @Test
    void shouldReturnPastBookingsByBookerIdTest() {
        createBookingDto(BookingStatus.APPROVED);
        List<BookingDtoResponse> bookings = bookingService.findAllByBookerId(BookingState.PAST,
                bookingDtoResponse.getBooker().getId(), 0, 10);
        List<Booking> checkBookings = entityManager.createQuery(
                        "SELECT booking " +
                                "FROM Booking booking " +
                                "WHERE booking.booker.id = :id AND booking.start < :now",
                        Booking.class)
                .setParameter("id", bookingDtoResponse.getBooker().getId())
                .setParameter("now", LocalDateTime.now())
                .getResultList();
        assertThat(bookings.get(0).getId(),
                equalTo(checkBookings.get(0).getId()));
        assertThat(bookings.size(),
                equalTo(checkBookings.size()));
        assertThat(bookings.size(),
                equalTo(1));
    }

    @Test
    void shouldReturnAllBookingsByOwnerIdTest() {
        createBookingDto(BookingStatus.REJECTED);

        List<BookingDtoResponse> bookings = bookingService.findAllByOwnerId(
                BookingState.ALL, owner.getId(), 0, 10);
        List<Booking> checkBookings = entityManager.createQuery(
                        "SELECT booking " +
                                "FROM Booking booking " +
                                "JOIN booking.item item " +
                                "WHERE item.owner.id = :id ",
                        Booking.class)
                .setParameter("id", owner.getId())
                .getResultList();
        assertThat(bookings.size(),
                equalTo(checkBookings.size()));
        assertThat(bookings.size(),
                equalTo(2));
    }


    @Test
    void shouldReturnFutureBookingsByOwnerIdTest() {
        List<BookingDtoResponse> bookings = bookingService.findAllByOwnerId(BookingState.FUTURE, owner.getId(), 0, 10);
        List<Booking> checkBookings = entityManager.createQuery(
                        "SELECT booking " +
                                "FROM Booking booking " +
                                "JOIN booking.item item " +
                                "WHERE item.owner.id = :id AND booking.start > :now",
                        Booking.class)
                .setParameter("id", owner.getId())
                .setParameter("now", LocalDateTime.now())
                .getResultList();
        assertThat(bookings.get(0).getId(),
                equalTo(checkBookings.get(0).getId()));
        assertThat(bookings.size(),
                equalTo(checkBookings.size()));
    }

    @Test
    void shouldReturnPastBookingsByOwnerIdTest() {
        createBookingDto(BookingStatus.APPROVED);

        List<BookingDtoResponse> bookings = bookingService.findAllByOwnerId(BookingState.PAST, owner.getId(), 0, 10);
        List<Booking> checkBookings = entityManager.createQuery(
                        "SELECT booking " +
                                "FROM Booking booking " +
                                "JOIN booking.item item " +
                                "WHERE item.owner.id = :id AND booking.start < :now",
                        Booking.class)
                .setParameter("id", owner.getId())
                .setParameter("now", LocalDateTime.now())
                .getResultList();
        assertThat(bookings.get(0).getId(),
                equalTo(checkBookings.get(0).getId()));
        assertThat(bookings.size(),
                equalTo(checkBookings.size()));
    }

    @Test
    void shouldReturnRejectBookingsByOwnerIdTest() {
        createBookingDto(BookingStatus.REJECTED);

        List<BookingDtoResponse> allBookings = bookingService.findAllByOwnerId(
                BookingState.REJECTED, owner.getId(), 0, 10);
        List<Booking> approved = entityManager.createQuery(
                        "SELECT booking " +
                                "FROM Booking booking " +
                                "JOIN booking.item item " +
                                "WHERE item.owner.id = :id AND booking.status = :status",
                        Booking.class)
                .setParameter("id", owner.getId())
                .setParameter("status", BookingStatus.REJECTED)
                .getResultList();
        assertThat(allBookings.size(),
                equalTo(approved.size()));
        assertThat(allBookings.size(),
                equalTo(1));
    }

    @Test
    void shouldReturnWaitingBookingsByOwnerIdTest() {
        createBookingDto(BookingStatus.WAITING);

        List<BookingDtoResponse> bookings = bookingService.findAllByOwnerId(BookingState.WAITING, owner.getId(), 0, 10);
        List<Booking> checkBookings = entityManager.createQuery(
                        "SELECT booking " +
                                "FROM Booking booking " +
                                "JOIN booking.item item " +
                                "WHERE item.owner.id = :id AND booking.status = :status",
                        Booking.class)
                .setParameter("id", owner.getId())
                .setParameter("status", BookingStatus.WAITING)
                .getResultList();
        assertThat(bookings.get(0).getId(),
                equalTo(checkBookings.get(0).getId()));
        assertThat(bookings.size(),
                equalTo(checkBookings.size()));
    }

    @Test
    void getBookingsByOwnerIdTest() {
        List<BookingDtoResponse> bookings = bookingService.findAllByOwnerId(BookingState.ALL, owner.getId(), 0, 10);
        List<Booking> checkBookings = entityManager.createQuery(
                        "SELECT booking " +
                                "FROM Booking booking " +
                                "JOIN booking.item item " +
                                "WHERE item.owner.id = :id",
                        Booking.class)
                .setParameter("id", owner.getId())
                .getResultList();
        assertThat(bookings.get(0).getId(),
                equalTo(checkBookings.get(0).getId()));
        assertThat(bookings.size(),
                equalTo(checkBookings.size()));
    }

    @Test
    void getBookingByIdTest() {
        BookingDtoResponse bookings = bookingService.getBooking(bookingDtoResponse.getId(),
                bookingDtoResponse.getBooker().getId());
        Booking checkBookings = entityManager
                .createQuery(
                        "SELECT booking " +
                                "FROM Booking booking " +
                                "WHERE booking.id = :id AND booking.booker.id = :bookerId",
                        Booking.class)
                .setParameter("bookerId", bookingDtoResponse.getBooker().getId())
                .setParameter("id", bookingDtoResponse.getId())
                .getSingleResult();
        assertThat(bookings.getItem().getId(),
                equalTo(checkBookings.getItem().getId()));
        assertThat(bookings.getStart(),
                equalTo(checkBookings.getStart()));
        assertThat(bookings.getId(),
                equalTo(checkBookings.getId()));
    }
}
