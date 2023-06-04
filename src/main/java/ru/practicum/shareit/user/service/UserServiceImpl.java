package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.user.repository.UserRepository;


import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDto create(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        UserDto responseDto = UserMapper.toUserDto(userRepository.save(user));
        log.info("Передаем в контроллер созданного пользователя : {}", responseDto);
        return responseDto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        List<UserDto> users = userRepository.findAll().stream().map(UserMapper::toUserDto)
                .collect(Collectors.toList());
        log.info("Передаем в контроллер список всех пользователей : {}", users);
        return users;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователь с id %d не существует", userId)));
        UserDto responseDto = UserMapper.toUserDto(user);
        log.info("Передаем в контроллер пользователя с id {} : {}", userId, responseDto);
        return responseDto;
    }

    @Override
    @Transactional
    public UserDto update(UserDto userDto, Long userId) {
        final User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователь с id %d не существует", userId)));

        String name = userDto.getName();
        String email = userDto.getEmail();
        if (name != null && !name.isBlank()) {
            user.setName(name);
        }
        if (email != null && !email.isBlank()) {
            user.setEmail(email);
        }
        User newUSer = userRepository.save(user);
        log.info("Передаем в контроллер обновленного пользователя с id {} : {}", userId, userDto);
        return UserMapper.toUserDto(newUSer);
    }

    @Override
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
        log.info("Удаляем пользователя c id {} ", id);
    }
}
