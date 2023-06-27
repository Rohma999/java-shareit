package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static ru.practicum.shareit.user.UserMapper.toUser;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    private UserService userService;
    private UserDto userDto;
    private User user;

    @BeforeEach
    void initialize() {
        userService = new UserServiceImpl(userRepository);
        userDto = UserDto.builder()
                .id(1L)
                .name("Pasha")
                .email("pasha@mail.com")
                .build();
        user = toUser(userDto);
    }

    @Test
    void createTest() {
        when(userRepository.save(any())).thenReturn(user);
        UserDto save = userService.create(userDto);

        assertEquals(save.getEmail(), user.getEmail());
        assertEquals(save.getName(), user.getName());
        assertEquals(save.getId(), user.getId());
    }

    @Test
    void saveUserSameEmailTest() {
        when(userRepository.save(any())).thenThrow(DataIntegrityViolationException.class);

        assertThrows(DataIntegrityViolationException.class, () -> userService.create(userDto));

    }

    @Test
    void updateUserNameTest() {
        UserDto userDto1 = UserDto.builder().id(1L).name("Vlad").email(null).build();
        UserDto userDto2 = UserDto.builder().id(1L)
                .name("Danil").email(userDto.getEmail()).build();
        when(userRepository.save(any())).thenReturn(user);
        userService.create(userDto);
        when(userRepository.save(any())).thenReturn(toUser(userDto2));
        when(userRepository.findById(any())).thenReturn(ofNullable(toUser(userDto2)));
        UserDto dto = userService.update(userDto1, userDto2.getId());

        assertNotEquals(dto.getEmail(), userDto1.getEmail());
        assertEquals(dto.getName(), userDto2.getName());
    }

    @Test
    void updateTest() {
        UserDto updatedUser = UserDto.builder().id(1L).name("Nadya").email("nadya@mail.com").build();
        when(userRepository.save(any()))
                .thenReturn(user);
        userService.create(userDto);
        when(userRepository.save(any()))
                .thenReturn(toUser(updatedUser));
        when(userRepository.findById(any()))
                .thenReturn(ofNullable(toUser(updatedUser)));
        UserDto dto = userService.update(updatedUser, updatedUser.getId());
        assertEquals(dto.getEmail(), updatedUser.getEmail());
        assertEquals(dto.getName(), updatedUser.getName());
    }

    @Test
    void getUserUserNotFoundTest() {
        when(userRepository.findById(any())).thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class, () -> userService.getUser(7L));
    }

    @Test
    void updateUserEmailTest() {
        UserDto userDto1 = UserDto.builder().id(1L).name(null).email("roma@mail.com").build();
        UserDto userDto2 = UserDto.builder().id(1L).name(userDto.getName()).email("roma@mail.com").build();
        when(userRepository.save(any())).thenReturn(user);
        when(userRepository.save(any())).thenReturn(toUser(userDto2));
        when(userRepository.findById(any())).thenReturn(ofNullable(toUser(userDto2)));
        userService.create(userDto);
        UserDto dto = userService.update(userDto1, userDto2.getId());

        assertNotEquals(dto.getName(), userDto1.getName());
        assertEquals(dto.getEmail(), userDto2.getEmail());
    }

    @Test
    void updateUserSameEmailTest() {
        UserDto dto = UserDto.builder().id(2L).name("Pasha").email("pasha@mail.com").build();
        when(userRepository.findById(any())).thenReturn(ofNullable(toUser(dto)));
        when(userRepository.save(any())).thenThrow(ValidationException.class);

        assertThrows(ValidationException.class, () -> userService.update(userDto, 1L));
    }

    @Test
    void deleteTest() {
        when(userRepository.save(any())).thenReturn(user);
        UserDto dto = userService.create(userDto);
        userService.deleteUserById(dto.getId());
        verify(userRepository, times(1)).deleteById(user.getId());
    }

    @Test
    void getUser999Test() {
        Exception exception = assertThrows(EntityNotFoundException.class, () -> userService.getUser(999L));

        assertEquals("Пользователь с id 999 не существует", exception.getMessage());
    }

    @Test
    void getAllEmptyTest() {
        when(userRepository.findAll()).thenReturn(List.of());
        List<UserDto> dtos = userService.getAllUsers();

        assertEquals(dtos.size(), 0);
    }

    @Test
    void getAllTest() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        List<UserDto> dtos = userService.getAllUsers();

        assertEquals(dtos.get(0).getId(), user.getId());
        assertEquals(dtos.get(0).getName(), user.getName());
        assertEquals(dtos.get(0).getEmail(), user.getEmail());
        assertEquals(dtos.size(), 1);
    }
}
