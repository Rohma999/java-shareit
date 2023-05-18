package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.exception.EmailAlreadyExistException;

import java.util.*;

@Repository
public class UserStorageImpl implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();

    private long id = 1;

    @Override
    public User create(User user) {
        if (emails.contains(user.getEmail())) {
            throw new EmailAlreadyExistException(
                    String.format("Пользователь с таким email %s уже существует", user.getEmail())
            );
        }
        user.setId(id++);
        users.put(user.getId(), user);
        emails.add(user.getEmail());
        return user;
    }

    @Override
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> update(User user, Long userId) {
        String email = user.getEmail();
        String name = user.getName();
        User userRep = users.get(userId);
        if (userRep != null && email != null) {
            if (!userRep.getEmail().equals(email) && emails.contains(email)) {
                throw new EmailAlreadyExistException(
                        String.format("Невозможно поменять email на %s,он уже занят", email)
                );
            }
            emails.remove(userRep.getEmail());
            userRep.setEmail(email);
            emails.add(email);
        }
        if (userRep != null && name != null) {
            userRep.setName(name);
        }
        return Optional.ofNullable(users.get(userId));
    }


    @Override
    public void deleteUserById(Long id) {
        if (users.containsKey(id)) {
            User user = users.remove(id);
            emails.remove(user.getEmail());
        }
    }
}
