package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.repository.BookingRepository;

import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;

import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;

import ru.practicum.shareit.item.repository.ItemRepository;

import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.user.UserMapper.toUser;


@ExtendWith(MockitoExtension.class)
class ItemServiceTest {
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    private ItemService itemService;
    private ItemDtoRequest itemDtoRequest;
    private UserDto userDto;
    private Item item;
    private User user;

    @BeforeEach
    void initialize() {
        itemService = new ItemServiceImpl(
                itemRepository,
                userRepository,
                bookingRepository,
                commentRepository,
                itemRequestRepository
        );
        userDto = UserDto.builder().id(1L).name("Eduard").email("ed@mail.com").build();
        user = UserMapper.toUser(userDto);
        itemDtoRequest = ItemDtoRequest.builder().name("Pocket").description("New Pocket")
                .available(true).build();
        item = Item.builder().id(1L).name("Pocket").description("New pocket").available(true).owner(toUser(userDto)).build();

    }

    private ItemDtoResponse saveItemDto() {
        when(userRepository.findById(anyLong()))
                .thenReturn(Optional.of(user));
        when(itemRepository.save(any()))
                .thenReturn(item);
        return itemService.create(itemDtoRequest, userDto.getId());
    }

    @Test
    void saveTest() {
        ItemDtoResponse saved = saveItemDto();

        assertEquals(saved.getName(), item.getName());
        assertEquals(saved.getId(), item.getId());
    }

    @Test
    void updateTest() {
        ItemDtoResponse dto = saveItemDto();
        Item updated = Item.builder().id(dto.getId()).name("Anton").description(itemDtoRequest.getDescription())
                .available(itemDtoRequest.getAvailable()).owner(toUser(userDto)).build();
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.ofNullable(item));
        when(itemRepository.save(any()))
                .thenReturn(updated);
        ItemDtoResponse update = itemService.update(userDto.getId(), 1L, itemDtoRequest);

        assertNotEquals(dto.getName(), update.getName());
        assertEquals(1L, update.getId());
    }

    @Test
    void searchTest() {
        saveItemDto();
        when(itemRepository.search(anyString(), any()))
                .thenReturn(of(item));
        List<ItemDtoResponse> search = itemService.search("oops", 0, 2);

        assertEquals(search.get(0).getId(), item.getId());
        assertEquals(search.size(), 1);
    }

    @Test
    void updateItemWithId999() {
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> itemService.update(1, 999, itemDtoRequest));

        assertEquals("Вещь с id 999 не существует", exception.getMessage());
    }


    @Test
    void saveCommentNotFoundItemTest() {
        CommentDtoRequest commentDto = CommentDtoRequest.builder().text("pink").build();
        when(itemRepository.findById(anyLong()))
                .thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class,
                () -> itemService.addComment(42, 2, commentDto));
    }

    @Test
    void searchEmptyTextTest() {
        List<ItemDtoResponse> search = itemService.search("", 0, 10);
        assertEquals(search.size(), 0);
    }

    @Test
    void getAllCommentsTest() {
        CommentDtoRequest commentDto = CommentDtoRequest.builder().text("space").build();
        Comment comment = Comment.builder().id(1L).text(commentDto.getText()).item(item)
                .author(user).created(now()).build();
        when(commentRepository.findAllByItemId(anyLong()))
                .thenReturn(of(comment));
        when(itemRepository.findById(anyLong()))
                .thenReturn(Optional.of(item));
        List<CommentDtoResponse> allComments = itemService.getItem(1, 1).getComments();

        assertEquals(allComments.get(0).getId(), comment.getId());
        assertEquals(allComments.size(), 1);
    }

    @Test
    void findAllTest() {
        Item newItem = Item.builder().id(2L).name("Bag").description("New bag")
                .available(true).owner(toUser(userDto)).build();
        when(itemRepository.findAllByOwnerIdOrderById(anyLong(), any())).thenReturn((of(item, newItem)));
        when(userRepository.findById(anyLong())).thenReturn((Optional.of(user)));
        List<ItemDtoResponse> items = itemService.getAllUserItems(1, 0, 10);

        assertEquals(2, items.size());
        assertEquals(items.get(0).getName(), item.getName());
        assertEquals(items.get(1).getName(), newItem.getName());
    }

}