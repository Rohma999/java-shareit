package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;

import java.time.LocalDateTime;
import java.util.Collection;

public interface BookingRepository extends JpaRepository<Booking,Long> {

    Collection<Booking> findAllByBookerIdOrderByStartDesc(long bookerId);

    Collection<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(long bookerId,LocalDateTime start);

    Collection<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(long bookerId, LocalDateTime end);

    Collection<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(long bookerId, LocalDateTime start,LocalDateTime end);

    Collection<Booking> findAllByBookerIdAndStatusOrderByStartDesc(long bookerId, BookingStatus status);

    Collection<Booking> findAllByItemOwnerIdOrderByStartDesc(long bookerId);

    Collection<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartDesc(long bookerId,LocalDateTime start);

    Collection<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(long bookerId, LocalDateTime end);

    Collection<Booking> findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(long bookerId, LocalDateTime start,LocalDateTime end);

    Collection<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(long bookerId, BookingStatus status);

    Collection<Booking> findAllByItemInAndStatusOrderByStartAsc(Collection<Item> items,BookingStatus status);

    Booking findFirstByItemIdAndStatusAndStartGreaterThanOrderByStart(long itemId, BookingStatus status, LocalDateTime startDate);

    Booking findFirstByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(long itemId, BookingStatus status, LocalDateTime startDate);
}

