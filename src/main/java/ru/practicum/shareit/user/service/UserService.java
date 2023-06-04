package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto create(UserDto userDto);

    List<UserDto> getAllUsers();

    UserDto getUser(Long id);

    UserDto update(UserDto user, Long userId);

    void deleteUserById(Long id);
}
