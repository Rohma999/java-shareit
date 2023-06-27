package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository, ItemRepository itemRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public BookingDtoResponse create(BookingDtoRequest bookingDtoRequest, long userId) {
        long itemId = bookingDtoRequest.getItemId();
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Вещь с id %d не существует", itemId))
        );
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователь с id %d не существует", userId))
        );
        if (!item.getAvailable()) {
            throw new ValidationException("Бронирование недоступной вещи запрещено");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new BookOwnItemsException("Бронировать собственные вещи запрещено");
        }
        if (!bookingDtoRequest.getEnd().isAfter(bookingDtoRequest.getStart())) {
            throw new ValidationException("Дата начала бронирования должна быть раньше даты возврата");
        }
        Booking booking = bookingRepository.save(BookingMapper.toBooking(bookingDtoRequest, user, item));
        log.info("Сохраняем в БД новое бронирование {}", booking);
        BookingDtoResponse bookingDtoResponse = BookingMapper.toBookingDtoResponse(booking);
        log.info("Возвращаем в контроллер созданное бронирование {}", bookingDtoResponse);
        return bookingDtoResponse;

    }

    @Override
    public BookingDtoResponse approve(long bookingId, Boolean approved, long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Бронирование с id %d не существует", bookingId))
        );
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователя с id %d не существует", userId))
        );
        if (!booking.getItem().getOwner().getId().equals(user.getId())) {
            throw new EntityNotFoundException("Подтвердить бронирование может только собственник вещи");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Вещь уже забронирована");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updateBooking = bookingRepository.save(booking);
        log.info("Сохраняем в БД обновленное бронирование {}", updateBooking);
        BookingDtoResponse bookingDtoResponse = BookingMapper.toBookingDtoResponse(updateBooking);
        log.info("Возвращаем в контроллер обновленное бронирование с новым статусом {}", bookingDtoResponse);
        return bookingDtoResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDtoResponse getBooking(long bookingId, long userId) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Бронирование с id %d не существует", bookingId))
        );
        userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователя с id %d не существует", userId))
        );
        if (!booking.getBooker().getId().equals(userId) && !booking.getItem().getOwner().getId().equals(userId)) {
            throw new EntityNotFoundException("Смотреть информацию о вещи может владелец вещи или автор бронирования");
        }
        BookingDtoResponse bookingDtoResponse = BookingMapper.toBookingDtoResponse(booking);
        log.info("Возвращаем в контроллер бронирование с id {} : {}", bookingId, bookingDtoResponse);
        return bookingDtoResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDtoResponse> findAllByBookerId(BookingState bookingState, long userId,int from,int size) {
        userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователя с id %d не существует", userId))
        );
        Collection<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();
        PageRequest pageRequest = PageRequest.of(from / size,size);
        switch (bookingState) {
            case CURRENT:
                bookings = bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now,pageRequest);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED,pageRequest);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING,pageRequest);
                break;
            case PAST:
                bookings = bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, now,pageRequest);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, now,pageRequest);
                break;
            case ALL:
                bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(userId,pageRequest);
                break;
            default:
                bookings = new ArrayList<>();
                break;
        }
        List<BookingDtoResponse> bookingsDto = bookings.stream().map(BookingMapper::toBookingDtoResponse)
                .collect(Collectors.toList());
        log.info("Возвращаем в контроллер бронирования со статусом {} пользователя с id {}  : {}",
                bookingState, userId, bookingsDto);
        return bookingsDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDtoResponse> findAllByOwnerId(BookingState bookingState, long userId,int from,int size) {
        userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователя с id %d не существует", userId))
        );
        Collection<Booking> bookings;
        LocalDateTime now = LocalDateTime.now();
        PageRequest pageRequest = PageRequest.of(from / size,size);
        switch (bookingState) {
            case CURRENT:
                bookings = bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now,pageRequest);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED,pageRequest);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING,pageRequest);
                break;
            case PAST:
                bookings = bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now,pageRequest);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now,pageRequest);
                break;
            case ALL:
                bookings = bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId,pageRequest);
                break;
            default:
                bookings = new ArrayList<>();
                break;
        }
        List<BookingDtoResponse> bookingsDto = bookings.stream().map(BookingMapper::toBookingDtoResponse)
                .collect(Collectors.toList());
        log.info("Возвращаем в контроллер бронирования со статусом {} пользователя с id {}  : {}",
                bookingState, userId, bookingsDto);
        return bookingsDto;
    }
}
