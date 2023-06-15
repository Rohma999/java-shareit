package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.request.dto.RequestItemRequestDto;
import ru.practicum.shareit.request.dto.ResponseItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceImplTest {
    private final ItemRequestService itemRequestService;
    private final EntityManager entityManager;
    private final UserService userService;
    private RequestItemRequestDto itemRequestDto;
    private UserDto user;

    @BeforeEach
    void initialize() {
        UserDto userDto = UserDto.builder().name("Artem").email("art@mail.com").build();
        user = userService.create(userDto);
        itemRequestDto = RequestItemRequestDto.builder().description("pen").build();
    }

    private void saveItemRequests() {
        RequestItemRequestDto itemRequestDto1 = RequestItemRequestDto.builder().description("ship").build();
        RequestItemRequestDto itemRequestDto2 = RequestItemRequestDto.builder().description("car").build();
        itemRequestService.create(itemRequestDto1, user.getId());
        itemRequestService.create(itemRequestDto2, user.getId());
    }

    @Test
    void saveItemRequestUserNotFoundTest() {
        final long id = 57L;
        Exception exception = assertThrows(EntityNotFoundException.class,
                () -> itemRequestService.create(itemRequestDto, id));
        Assertions.assertEquals("Пользователь с id " + id + " не существует", exception.getMessage());
    }

    @Test
    void saveItemRequestTest() {
        itemRequestService.create(itemRequestDto, user.getId());
        ItemRequest itemRequest = entityManager.createQuery(
                "SELECT itemRequest " +
                        "FROM ItemRequest itemRequest",
                ItemRequest.class).getSingleResult();
        assertThat(itemRequest.getDescription(), equalTo(itemRequestDto.getDescription()));
        assertThat(itemRequest.getId(), notNullValue());
    }

    @Test
    void getAllItemRequestsTest() {
        saveItemRequests();
        List<ResponseItemRequestDto> allItemRequests = itemRequestService.findAllByUserId(user.getId());
        List<ItemRequest> itemRequests = entityManager.createQuery(
                        "SELECT itemRequest " +
                                "FROM ItemRequest itemRequest " +
                                "WHERE itemRequest.requester.id = :id " +
                                "ORDER BY itemRequest.created DESC ",
                        ItemRequest.class)
                .setParameter("id", user.getId())
                .getResultList();
        assertThat(allItemRequests.get(0).getId(), equalTo(itemRequests.get(0).getId()));
        assertThat(allItemRequests.size(), equalTo(itemRequests.size()));
    }

    @Test
    void getItemRequestByIdTest() {
        itemRequestService.create(itemRequestDto, user.getId());
        ItemRequest itemRequest = entityManager.createQuery(
                "SELECT itemRequest " +
                        "FROM ItemRequest itemRequest",
                ItemRequest.class).getSingleResult();
        ResponseItemRequestDto itemRequestById = itemRequestService.getById(itemRequest.getId(), user.getId());
        assertThat(itemRequestById.getDescription(), equalTo(itemRequest.getDescription()));
        assertThat(itemRequestById.getId(), equalTo(itemRequest.getId()));
    }

    @Test
    void getAllItemRequestsByOwnerTest() {
        saveItemRequests();
        List<ResponseItemRequestDto> allItemRequests = itemRequestService.findAll(user.getId(), 0, 10);
        List<ItemRequest> itemRequests = entityManager.createQuery(
                        "SELECT itemRequest " +
                                "FROM ItemRequest itemRequest " +
                                "WHERE itemRequest.requester.id <> :id " +
                                "ORDER BY itemRequest.created DESC ",
                        ItemRequest.class)
                .setParameter("id", user.getId())
                .getResultList();
        assertThat(allItemRequests.size(), equalTo(itemRequests.size()));
        assertThat(allItemRequests.size(), equalTo(0));
    }

    @Test
    void getAllItemRequests2Test() {
        saveItemRequests();
        UserDto userDto = userService.create(UserDto.builder().name("Tolik").email("to@mail.com").build());
        List<ResponseItemRequestDto> allItemRequests = itemRequestService.findAll(userDto.getId(), 0, 10);
        List<ItemRequest> itemRequests = entityManager.createQuery(
                        "SELECT itemRequest " +
                                "FROM ItemRequest itemRequest " +
                                "WHERE itemRequest.requester.id <> :id " +
                                "ORDER BY itemRequest.created DESC ",
                        ItemRequest.class)
                .setParameter("id", userDto.getId())
                .getResultList();
        assertThat(allItemRequests.get(0).getId(), equalTo(itemRequests.get(0).getId()));
        assertThat(allItemRequests.size(), equalTo(itemRequests.size()));
    }
}