package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;

import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.NotOwnerException;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static java.time.LocalDateTime.now;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplTest {
    private final BookingService bookingService;
    private final EntityManager entityManager;
    private final UserService userService;
    private final ItemService itemService;
    private UserDto userDto;
    private ItemDtoResponse itemDto;

    @BeforeEach
    void initialize() {
        userDto = userService.create(UserDto.builder().name("Roma").email("roma@mail.com").build());
        itemDto = itemService.create(ItemDtoRequest.builder().name("Pen").description("blue pen").available(true)
                .build(), userDto.getId());

    }

    private CommentDtoResponse saveCommentDto(String commentText, UserDto user) {
        UserDto booker = userService.create(user);
        BookingDtoRequest bookingDtoRequest = BookingDtoRequest.builder().start(now().minusSeconds(2))
                .end(now().minusSeconds(1)).itemId(itemDto.getId()).build();
        BookingDtoResponse bookingDtoResponse = bookingService.create(bookingDtoRequest, booker.getId());
        bookingService.approve(bookingDtoResponse.getId(), true, userDto.getId());
        CommentDtoRequest commentDtoRequest = CommentDtoRequest.builder().text(commentText).build();
        return itemService.addComment(booker.getId(), itemDto.getId(), commentDtoRequest);
    }

    @Test
    void saveTest() {
        Item item = entityManager.createQuery(
                "SELECT item " +
                        "FROM Item item",
                Item.class).getSingleResult();
        assertThat(item.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(item.getAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(item.getName(), equalTo(itemDto.getName()));
        assertThat(item.getId(), notNullValue());
    }

    @Test
    void updateTest() {
        ItemDtoRequest dto = ItemDtoRequest.builder()
                .id(itemDto.getId()).name("Bear").description("soft bear").available(false).build();
        itemService.update(userDto.getId(), dto.getId(), dto);
        Item item = entityManager.createQuery(
                "SELECT item " +
                        "FROM Item item",
                Item.class).getSingleResult();
        assertThat(item.getDescription(), equalTo(dto.getDescription()));
        assertThat(item.getAvailable(), equalTo(dto.getAvailable()));
        assertThat(item.getName(), equalTo(dto.getName()));
        assertThat(item.getId(), notNullValue());
    }

    @Test
    void getTest() {
        ItemDtoResponse itemDtoResponse = itemService.getItem(itemDto.getId(), userDto.getId());
        Item item = entityManager.createQuery(
                        "SELECT item " +
                                "FROM Item item " +
                                "WHERE item.id = :id " +
                                "AND item.owner.id = :ownerId",
                        Item.class)
                .setParameter("ownerId", userDto.getId())
                .setParameter("id", itemDto.getId())
                .getSingleResult();
        assertThat(item.getDescription(), equalTo(itemDtoResponse.getDescription()));
        assertThat(item.getAvailable(), equalTo(itemDtoResponse.getAvailable()));
        assertThat(item.getName(), equalTo(itemDtoResponse.getName()));
        assertThat(item.getId(), notNullValue());
    }

    @Test
    void getAllTest() {
        ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder().name("Doll").description("cute doll")
                .available(true).build();
        itemDto = itemService.create(itemDtoRequest, userDto.getId());
        List<ItemDtoResponse> allItems = itemService.getAllUserItems(userDto.getId(), 0, 2);
        List<Item> items = entityManager.createQuery(
                        "SELECT item " +
                                "FROM Item item " +
                                "WHERE item.owner.id = :ownerId",
                        Item.class)
                .setParameter("ownerId", userDto.getId())
                .getResultList();
        assertThat(items.get(0).getId(), equalTo(allItems.get(0).getId()));
        assertThat(items.size(), equalTo(allItems.size()));
    }

    @Test
    void searchNotAvailableItemTest() {
        ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder().name("Truck").description("Big truck")
                .available(false).build();
        itemDto = itemService.create(itemDtoRequest, userDto.getId());
        List<ItemDtoResponse> itemsDto = itemService.search("truck", 0, 2);
        List<Item> items = entityManager.createQuery(
                        "SELECT item " +
                                "FROM Item item " +
                                "WHERE item.available = TRUE AND (UPPER(item.name) LIKE UPPER(CONCAT('%', :text, '%')) " +
                                "OR UPPER(item.description) LIKE UPPER(CONCAT('%', :text, '%')))",
                        Item.class)
                .setParameter("text", "car")
                .getResultList();
        assertThat(items.size(), equalTo(itemsDto.size()));
        assertThat(items, empty());
    }

    @Test
    void searchTest() {
        ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder().name("Car").description("Red car")
                .available(true).build();
        itemDto = itemService.create(itemDtoRequest, userDto.getId());
        List<ItemDtoResponse> itemsDto = itemService.search("car", 0, 2);
        List<Item> items = entityManager.createQuery(
                        "SELECT item " +
                                "FROM Item item " +
                                "WHERE item.available = TRUE AND (UPPER(item.name) LIKE UPPER(CONCAT('%', :text, '%')) " +
                                "OR UPPER(item.description) LIKE UPPER(CONCAT('%', :text, '%')))",
                        Item.class)
                .setParameter("text", "car")
                .getResultList();
        assertThat(items.get(0).getId(), equalTo(itemsDto.get(0).getId()));
        assertThat(items.size(), equalTo(itemsDto.size()));
    }

    @Test
    void getAllCommentsTest() {
        saveCommentDto("Winter", UserDto.builder().name("David").email("dav@mail.com").build());
        saveCommentDto("Spring", UserDto.builder().name("Vanya").email("van@mail.com").build());

        ItemDtoResponse itemDtoResponse = itemService.getItem(itemDto.getId(), userDto.getId());
        List<Comment> comments = entityManager.createQuery(
                        "SELECT comment " +
                                "FROM Comment comment",
                        Comment.class)
                .getResultList();

        List<Booking> bookings = entityManager.createQuery(
                        "SELECT bookings " +
                                "FROM Booking  bookings",
                        Booking.class)
                .getResultList();

        List<CommentDtoResponse> allComments = itemDtoResponse.getComments();

        assertThat(bookings.get(1).getId(), equalTo(itemDtoResponse.getLastBooking().getId()));
        assertThat(comments.size(), equalTo(allComments.size()));
        assertThat(comments.size(), equalTo(2));
        assertThat(comments, notNullValue());
    }

    @Test
    void getEntityNotFoundExceptionTest() {
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> itemService.getItem(7, userDto.getId()));
        assertEquals("Вещь с id 7 не существует", exception.getMessage());
    }

    @Test
    void updateWhenNotOwnerTest() {
        ItemDtoRequest dto = ItemDtoRequest.builder()
                .id(itemDto.getId()).name("Bear").description("soft bear").available(false).build();
        Exception exception = assertThrows(NotOwnerException.class,
                () -> itemService.update(999, dto.getId(), dto));
        assertEquals("Вы не являетесь владельцем данной вещи", exception.getMessage());
    }
}