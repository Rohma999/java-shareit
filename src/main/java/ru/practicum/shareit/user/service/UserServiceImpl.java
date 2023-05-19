package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserServiceImpl(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public UserDto create(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        UserDto responseDto = UserMapper.toUserDto(userStorage.create(user));
        log.info("Передаем в контроллер созданного пользователя : {}", responseDto);
        return responseDto;
    }

    @Override
    public Collection<UserDto> getAllUsers() {
        Collection<UserDto> users = userStorage.getAllUsers().stream()
                .map(UserMapper::toUserDto).collect(Collectors.toList());
        log.info("Передаем в контроллер список всех пользователей : {}", users);
        return users;
    }

    @Override
    public UserDto getUser(Long userId) {
        User user = userStorage.getUser(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователь с id %d не существует", userId)));
        UserDto responseDto = UserMapper.toUserDto(user);
        log.info("Передаем в контроллер пользователя с id {} : {}", userId, responseDto);
        return responseDto;
    }

    @Override
    public UserDto update(UserDto userDto, Long userId) {
        final User user = userStorage.getUser(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователь с id %d не существует", userId)));

        if (userDto.getName() == null) {
            userDto.setName(user.getName());
        }
        if (userDto.getEmail() == null) {
            userDto.setEmail(user.getEmail());
        }
        User newUser = userStorage.update(UserMapper.toUser(userDto),userId);
        UserDto responseDto = UserMapper.toUserDto(newUser);
        log.info("Передаем в контроллер обновленного пользователя с id {} : {}", userId, responseDto);
        return responseDto;
    }

    @Override
    public void deleteUserById(Long id) {
        userStorage.deleteUserById(id);
        log.info("Удаляем пользователя c id {} ", id);
    }
}
