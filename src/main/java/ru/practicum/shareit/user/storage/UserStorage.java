package ru.practicum.shareit.user.storage;

import ru.practicum.shareit.user.model.User;


import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    User create(User user);

    Collection<User> getAllUsers();

    Optional<User> findById(Long id);

    Optional<User> update(User user,Long userId);

    void deleteUserById(Long id);

}
