package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.NotOwnerException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    private final ItemRequestRepository itemRequestRepository;

    @Autowired
    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository,
                           BookingRepository bookingRepository, CommentRepository commentRepository,
                           ItemRequestRepository itemRequestRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.itemRequestRepository = itemRequestRepository;
    }

    @Override
    public ItemDtoResponse create(ItemDtoRequest itemDto, long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователь с id %d не существует", userId)));
        Long requestId = itemDto.getRequestId();
        ItemRequest itemRequest = null;
        if (requestId != null) {
            itemRequest = itemRequestRepository.findById(requestId)
                    .orElseThrow(() -> new EntityNotFoundException("Запрос с идентификатором " + requestId + " не найден."));
        }
        Item item = itemRepository.save(ItemMapper.toItem(itemDto, user, itemRequest));
        log.info("Сохраняем в БД вещь : {}", item);
        ItemDtoResponse responseDto = ItemMapper.toItemDtoResponse(item, new ArrayList<>());
        log.info("Передаем в контроллер созданную вещь : {}", responseDto);
        return responseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDtoResponse> getAllUserItems(long userId, int from, int size) {
        userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователь с id %d не существует", userId)));
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Item> items = itemRepository.findAllByOwnerIdOrderById(userId, pageRequest);
        Map<Item, List<Booking>> bookings = getBookings(items);

        List<ItemDtoResponse> itemDtoResponses = new ArrayList<>();
        for (Item item : items) {
            List<CommentDtoResponse> comment = getComments(item.getId());
            ItemDtoResponse itemDto = ItemMapper.toItemDtoResponse(item, comment);
            setBookings(itemDto, bookings.get(item), LocalDateTime.now());
            itemDtoResponses.add(itemDto);
        }
        log.info("Передаем в контроллер список вещей пользователя с id {} : {}", userId, itemDtoResponses);
        return itemDtoResponses;
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDtoResponse getItem(long itemId, long userId) {
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Вещь с id %d не существует", itemId))
        );
        User user = item.getOwner();
        List<CommentDtoResponse> comments = getComments(itemId);
        ItemDtoResponse responseDto;
        if (!user.getId().equals(userId)) {
            responseDto = ItemMapper.toItemDtoResponse(item, comments);
            log.info("Передаем в контроллер вещь  с id {} : {}", itemId, responseDto);
            return responseDto;
        }
        LocalDateTime now = LocalDateTime.now();
        Booking last = bookingRepository.findFirstByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(
                item.getId(), BookingStatus.APPROVED, now
        );
        Booking next = bookingRepository.findFirstByItemIdAndStatusAndStartGreaterThanOrderByStart(
                item.getId(), BookingStatus.APPROVED, now
        );
        responseDto = ItemMapper.toItemDtoResponse(item, comments);
        if (next != null) {
            responseDto.setNextBooking(new ItemDtoResponse.BookingDtoShort(next.getId(), next.getBooker().getId()));
        }
        if (last != null) {
            responseDto.setLastBooking(new ItemDtoResponse.BookingDtoShort(last.getId(), last.getBooker().getId()));
        }
        log.info("Передаем в контроллер вещь  с id {} : {}", itemId, responseDto);
        return responseDto;
    }

    @Override
    public ItemDtoResponse update(long userId, long itemId, ItemDtoRequest itemDtoRequest) {
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Вещь с id %d не существует", itemId))
        );
        if (item.getOwner().getId() != userId) {
            throw new NotOwnerException("Вы не являетесь владельцем данной вещи");
        }
        String name = itemDtoRequest.getName();
        String description = itemDtoRequest.getDescription();
        Boolean available = itemDtoRequest.getAvailable();
        if (name != null && !name.isBlank()) {
            item.setName(name);
        }
        if (description != null && !description.isBlank()) {
            item.setDescription(description);
        }
        if (available != null) {
            item.setAvailable(available);
        }
        List<CommentDtoResponse> comments = getComments(itemId);
        Item updateItem = itemRepository.save(item);
        log.info("Обновляем в БД вещь : {}", updateItem);
        ItemDtoResponse responseDto = ItemMapper.toItemDtoResponse(updateItem, comments);
        log.info("Передаем в контроллер обновленную вещь с id {} : {}", itemId, responseDto);
        return responseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDtoResponse> search(String text, int from, int size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<ItemDtoResponse> items = itemRepository.search(text, pageRequest)
                .stream().map(item -> {
                    List<CommentDtoResponse> comments = getComments(item.getId());
                    return ItemMapper.toItemDtoResponse(item, comments);
                }).collect(Collectors.toList());
        log.info("Передаем в контроллер найденные вещи : {} ", items);
        return items;
    }

    @Override
    public CommentDtoResponse addComment(long userId, long itemId, CommentDtoRequest commentDtoRequest) {
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Вещь с id %d не существует", itemId))
        );
        User author = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователь с id %d не существует", userId)));
        Collection<Booking> bookings = bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(
                author.getId(), LocalDateTime.now()
        );
        boolean checkBooking = bookings.stream().anyMatch(booking -> booking.getItem().getId() == itemId);
        if (!checkBooking) {
            throw new ValidationException("Нет прав на добавление комментария");
        }
        Comment comment = commentRepository.save(CommentMapper.toComment(commentDtoRequest, author, item, LocalDateTime.now()));
        log.info("Сохраняем в БД отзыв : {}", comment);
        CommentDtoResponse commentDtoResponse = CommentMapper.toCommentDtoResponse(comment);
        log.info("Передаем в контроллер созданный отзыв : {} ", commentDtoResponse);
        return commentDtoResponse;
    }

    private Map<Item, List<Booking>> getBookings(List<Item> items) {
        return bookingRepository.findAllByItemInAndStatusOrderByStartAsc(items, BookingStatus.APPROVED)
                .stream().collect(Collectors.groupingBy(Booking::getItem, Collectors.toList()));
    }

    private void setBookings(ItemDtoResponse itemDto, List<Booking> bookings, LocalDateTime now) {
        Booking lastBooking = bookings == null ? null :
                bookings.stream().filter(booking -> booking.getStart().isBefore(now))
                        .reduce((first, second) -> second).orElse(null);

        Booking nextBooking = bookings == null ? null :
                bookings.stream().filter(booking -> booking.getStart().isAfter(now))
                        .findFirst().orElse(null);

        itemDto.setLastBooking(lastBooking == null ? null :
                new ItemDtoResponse.BookingDtoShort(lastBooking.getId(), lastBooking.getBooker().getId()));
        itemDto.setNextBooking(nextBooking == null ? null :
                new ItemDtoResponse.BookingDtoShort(nextBooking.getId(), nextBooking.getBooker().getId()));
    }

    private List<CommentDtoResponse> getComments(long itemId) {
        return commentRepository.findAllByItemId(itemId).stream()
                .map(CommentMapper::toCommentDtoResponse).collect(Collectors.toList());
    }
}
