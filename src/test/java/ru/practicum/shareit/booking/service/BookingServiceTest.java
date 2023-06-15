package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BookOwnItemsException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RequiredArgsConstructor
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    private static final long USER_ID = 1L;
    private static final long USER_ID2 = 2L;
    private static final long USER_ID3 = 3L;
    private static final long ITEM_ID = 1L;
    private static final long BOOKING_ID = 1L;

    private final LocalDateTime bookingStartDate = LocalDateTime.now();
    private final LocalDateTime bookingEndDate = bookingStartDate.plusDays(1);
    private final BookingDtoRequest addBookingDto = BookingDtoRequest.builder().start(bookingStartDate)
            .end(bookingEndDate).itemId(ITEM_ID).build();

    private BookingService bookingService;

    @Mock
    BookingRepository bookingRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ItemRepository itemRepository;

    @BeforeEach
    void initialize() {
        bookingService = new BookingServiceImpl(
                bookingRepository,
                itemRepository,
                userRepository

        );
    }

    @Test
    void createBookingWhenItemIsNotAvailableThenThrowValidationException() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(User.builder().build()));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(Item.builder().available(false).build()));
        final ValidationException exception = assertThrows(
                ValidationException.class,
                () -> bookingService.create(addBookingDto, USER_ID)
        );

        assertEquals("Бронирование недоступной вещи запрещено", exception.getMessage());
    }

    @Test
    void createBookingWhenBookerIsOwnerThrowBookOwnItemsException() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(User.builder().id(USER_ID).build()));

        User owner = User.builder().id(USER_ID).build();
        Item item = Item.builder().available(true).owner(owner).build();
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(item));

        final BookOwnItemsException exception = assertThrows(
                BookOwnItemsException.class,
                () -> bookingService.create(addBookingDto, USER_ID)
        );

        assertEquals("Бронировать собственные вещи запрещено", exception.getMessage());
    }

    @Test
    void getByIdWhenUserNotFoundThrowEntityNotFoundException() {
        Booking booking = Booking.builder().build();
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.getBooking(BOOKING_ID, USER_ID)
        );

        assertEquals("Пользователя с id " + USER_ID + " не существует", exception.getMessage());
    }

    @Test
    void getByIdWhenBookingNotFoundThrowEntityNotFoundException() {
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.getBooking(BOOKING_ID, USER_ID)
        );

        assertEquals("Бронирование с id " + BOOKING_ID + " не существует", exception.getMessage());
    }

    @Test
    void getByIdWhenUserIsNotBookerOrOwnerThrowEntityNotFoundException() {
        User booker = User.builder().id(USER_ID).build();
        User owner = User.builder().id(USER_ID2).build();
        Item item = Item.builder().owner(owner).build();
        Booking booking = Booking.builder().id(BOOKING_ID).start(LocalDateTime.now()).end(LocalDateTime.now()
                .plusHours(1)).booker(booker).item(item).status(BookingStatus.WAITING).build();
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

        long wrongUserId = USER_ID + USER_ID2;
        User wrongUser = User.builder().id(wrongUserId).build();
        when(userRepository.findById(wrongUserId)).thenReturn(Optional.of(wrongUser));

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.getBooking(BOOKING_ID, wrongUserId)
        );

        assertEquals("Смотреть информацию о вещи может владелец вещи или автор бронирования",
                exception.getMessage());
    }


    @Test
    void approvedWhenUserIdIsNotOwnerIdThrowEntityNotFoundException() {
        User booker = User.builder().id(USER_ID).build();
        User owner = User.builder().id(USER_ID2).build();
        Item item = Item.builder().owner(owner).build();
        Booking booking = Booking.builder().id(BOOKING_ID).start(LocalDateTime.now()).end(LocalDateTime.now()
                .plusHours(1)).booker(booker).item(item).status(BookingStatus.WAITING).build();
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(booker));

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.approve(BOOKING_ID, true, USER_ID)
        );

        assertEquals("Подтвердить бронирование может только собственник вещи",
                exception.getMessage());
    }

    @Test
    void approvedWhenRejected() {
        User booker = User.builder().id(USER_ID3).build();
        User ownerOfItem = User.builder().id(USER_ID).build();
        Item item = Item.builder().owner(ownerOfItem).name("Pen").build();
        Booking booking = Booking.builder().id(BOOKING_ID).start(LocalDateTime.now()).end(LocalDateTime.now()
                .plusHours(1)).booker(booker).item(item).status(BookingStatus.WAITING).build();
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

        User owner = User.builder().id(USER_ID).build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(owner));

        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingDtoResponse bookingDto = bookingService.approve(BOOKING_ID, false, USER_ID);

        assertThat(bookingDto.getStatus(), equalTo(BookingStatus.REJECTED));
    }

    @Test
    void approveWhenBookingNotFoundThrowEntityNotFoundException() {
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());
        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.approve(BOOKING_ID, true, USER_ID)
        );

        assertEquals("Бронирование с id " + BOOKING_ID + " не существует", exception.getMessage());
    }

    @Test
    void approvedWhenUserNotFoundThenThrowEntityNotFoundException() {
        Booking booking = Booking.builder().status(BookingStatus.WAITING).build();
        when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.approve(BOOKING_ID, true, USER_ID)
        );

        assertEquals("Пользователя с id " + USER_ID + " не существует", exception.getMessage());
    }

    @Test
    void findAllByOwnerIdWhenUserNotFoundThenThrowEntityNotFoundException() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.findAllByOwnerId(BookingState.ALL, USER_ID, 0, 10)
        );

        assertEquals("Пользователя с id " + USER_ID + " не существует", exception.getMessage());
    }

    @Test
    void findAllByBookerIdWhenUserNotFoundThenThrowEntityNotFoundException() {

        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookingService.findAllByBookerId(BookingState.ALL, USER_ID, 0, 10)
        );

        assertEquals("Пользователя с id " + USER_ID + " не существует", exception.getMessage());
    }
}