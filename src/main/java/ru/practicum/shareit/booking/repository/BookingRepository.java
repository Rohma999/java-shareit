package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBookerIdOrderByStartDesc(long bookerId);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(long bookerId, LocalDateTime start);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(long bookerId, LocalDateTime end);

    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(long bookerId, LocalDateTime start, LocalDateTime end);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(long bookerId, BookingStatus status);

    List<Booking> findAllByItemOwnerIdOrderByStartDesc(long bookerId);

    List<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartDesc(long bookerId, LocalDateTime start);

    List<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(long bookerId, LocalDateTime end);

    List<Booking> findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(long bookerId, LocalDateTime start, LocalDateTime end);

    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(long bookerId, BookingStatus status);

    List<Booking> findAllByItemInAndStatusOrderByStartAsc(List<Item> items, BookingStatus status);

    Booking findFirstByItemIdAndStatusAndStartGreaterThanOrderByStart(long itemId, BookingStatus status, LocalDateTime startDate);

    Booking findFirstByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(long itemId, BookingStatus status, LocalDateTime startDate);
}

