package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.RequestItemRequestDto;
import ru.practicum.shareit.request.repository.ItemRequestRepository;

import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@RequiredArgsConstructor
@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {

    private static final long USER_ID = 1L;
    private static final long REQUEST_ID = 1L;
    private RequestItemRequestDto request;
    @Mock
    ItemRequestRepository itemRequestRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    ItemRepository itemRepository;

    ItemRequestService itemRequestService;

    @BeforeEach
    void setUp() {
        itemRequestService = new ItemRequestServiceImpl(userRepository, itemRepository, itemRequestRepository);
        request = RequestItemRequestDto.builder().description("description").build();
    }


    @Test
    void createItemRequestWhenUserNotExistsThenThrowException() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> itemRequestService.create(request, USER_ID)
        );

        assertEquals("Пользователь с id " + USER_ID + " не существует", exception.getMessage());
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(USER_ID);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findAllByRequesterIdWhenUserNotExistsThenThrowException() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> itemRequestService.findAllByUserId(USER_ID)
        );

        assertEquals("Пользователь с id " + USER_ID + " не существует", exception.getMessage());
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(USER_ID);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void findAllWhenUserNotExistsThenThrowException() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        int from = 0;
        int size = 10;
        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> itemRequestService.findAll(USER_ID, from, size)
        );

        assertEquals("Пользователь с id " + USER_ID + " не существует", exception.getMessage());
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(USER_ID);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getByIdWhenUserNotExistsThenThrowException() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> itemRequestService.getById(REQUEST_ID, USER_ID)
        );

        assertEquals("Пользователь с id " + USER_ID + " не существует", exception.getMessage());
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(USER_ID);
        Mockito.verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getByIdWhenItemRequestNotExistsThenThrowException() {
        User user = User.builder().build();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        when(itemRequestRepository.findById(REQUEST_ID)).thenReturn(Optional.empty());

        final EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> itemRequestService.getById(REQUEST_ID, USER_ID)
        );

        assertEquals("Запрос с id " + REQUEST_ID + " не существует", exception.getMessage());
        Mockito.verify(userRepository, Mockito.times(1))
                .findById(USER_ID);
        Mockito.verify(itemRequestRepository, Mockito.times(1))
                .findById(REQUEST_ID);
        Mockito.verifyNoMoreInteractions(userRepository);
        Mockito.verifyNoMoreInteractions(itemRequestRepository);
    }
}