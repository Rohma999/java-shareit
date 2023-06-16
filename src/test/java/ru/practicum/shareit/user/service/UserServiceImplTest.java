package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;


import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplTest {
    private final EntityManager entityManager;
    private final UserService userService;
    private UserDto userDto;

    @BeforeEach
    void initialize() {
        userDto = saveUserDto("Nik", "Nik@mail.com");
    }

    private UserDto saveUserDto(String name, String email) {
        return UserDto.builder().id(null).name(name).email(email).build();
    }

    private void addUsers() {
        userService.create(saveUserDto("Roma", "Roma@mail.com"));
        userService.create(saveUserDto("Andrey", "Andrey@mail.com"));
        userService.create(saveUserDto("Vlad", "Vlad@mail.com"));
    }

    @Test
    void shouldNotNullTest() {
        UserDto dto = userService.create(saveUserDto("Gena", "Gena@mail.com"));
        assertNotEquals(null, dto);
    }

    @Test
    void shouldGetTest() {
        userService.create(userDto);
        User user = entityManager.createQuery(
                        "SELECT user " +
                                "FROM User user " +
                                "WHERE user.email = :email",
                        User.class)
                .setParameter("email", userDto.getEmail())
                .getSingleResult();
        UserDto userDtoFrom = userService.getUser(user.getId());
        assertThat(userDtoFrom.getEmail(), equalTo(user.getEmail()));
        assertThat(userDtoFrom.getName(), equalTo(user.getName()));
        assertThat(userDtoFrom.getId(), equalTo(user.getId()));
    }

    @Test
    void shouldUpdateTest() {
        userService.create(userDto);
        User user = entityManager.createQuery(
                        "SELECT user " +
                                "FROM User user " +
                                "WHERE user.email = :email",
                        User.class)
                .setParameter("email", userDto.getEmail())
                .getSingleResult();
        UserDto dto = saveUserDto("Liza", "Liza@mail.com");
        userService.update(dto, user.getId());
        User updatedUser = entityManager.createQuery(
                        "SELECT user " +
                                "FROM User user " +
                                "WHERE user.id = :id",
                        User.class)
                .setParameter("id", user.getId())
                .getSingleResult();
        assertThat(updatedUser.getEmail(), equalTo(dto.getEmail()));
        assertThat(updatedUser.getName(), equalTo(dto.getName()));
        assertThat(updatedUser.getId(), notNullValue());
    }

    @Test
    void shouldCreateTest() {
        userService.create(userDto);
        User user = entityManager.createQuery(
                        "SELECT user " +
                                "FROM User user " +
                                "WHERE user.email = :email",
                        User.class)
                .setParameter("email", userDto.getEmail())
                .getSingleResult();
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getId(), notNullValue());
    }

    @Test
    void shouldDeleteTest() {
        addUsers();
        List<User> usersBefore = entityManager.createQuery(
                "SELECT user " +
                        "FROM User user",
                User.class).getResultList();
        assertThat(usersBefore.size(), equalTo(3));
        userService.deleteUserById(usersBefore.get(0).getId());
        List<User> usersAfter = entityManager.createQuery(
                "SELECT user " +
                        "FROM User user",
                User.class).getResultList();
        assertThat(usersAfter.size(), equalTo(2));
    }

    @Test
    void shouldThrowEmailExceptionTest() {
        userService.create(saveUserDto("Jack", "jack@mail.com"));
        Exception exception = assertThrows(DataIntegrityViolationException.class,
                () -> userService.create(saveUserDto("Jack", "jack@mail.com")));
        assertEquals("could not execute statement; SQL [n/a]; constraint [null]; nested exception " +
                "is org.hibernate.exception.ConstraintViolationException: could not execute statement", exception.getMessage());
    }

    @Test
    void shouldGetAllTest() {
        addUsers();
        List<User> users = entityManager.createQuery(
                "SELECT user " +
                        "FROM User user",
                User.class).getResultList();
        assertThat(users.size(), equalTo(3));
    }
}