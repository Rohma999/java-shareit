package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.group.Create;
import ru.practicum.shareit.group.Update;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(path = "/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserDto create(@Validated(Create.class) @RequestBody UserDto userDto) {
        log.info("Запрос на добавление пользователя {}", userDto);
        return userService.create(userDto);
    }

    @PatchMapping("{userId}")
    public UserDto patch(@Validated(Update.class) @RequestBody UserDto userDto, @PathVariable long userId) {
        log.info("Запрос на изменение пользователя с id {}, данные для замены {}", userId, userDto);
        return userService.update(userDto, userId);
    }

    @GetMapping
    public Collection<UserDto> findAll() {
        log.info("Запрос на получение всех пользователей");
        return userService.getAllUsers();
    }

    @GetMapping("{userId}")
    public UserDto findById(@PathVariable long userId) {
        log.info("Запрос на получение пользователя с id {}", userId);
        return userService.findById(userId);
    }

    @DeleteMapping("{userId}")
    public void deleteUserById(@PathVariable long userId) {
        log.info("Запрос на удаление пользователя с id {}", userId);
        userService.deleteUserById(userId);
    }
}
