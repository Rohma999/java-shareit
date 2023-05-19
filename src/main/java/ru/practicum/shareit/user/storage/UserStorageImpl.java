package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.exception.EmailAlreadyExistException;

import java.util.*;

@Repository
public class UserStorageImpl implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();

    private long counterId = 0;

    @Override
    public User create(User user) {
        if (emails.contains(user.getEmail())) {
            throw new EmailAlreadyExistException(
                    String.format("Пользователь с таким email %s уже существует", user.getEmail())
            );
        }
        user.setId(++counterId);
        users.put(user.getId(), user);
        emails.add(user.getEmail());
        return user;
    }

    @Override
    public Collection<User> getAllUsers() {
        return users.values();
    }

    @Override
    public Optional<User> getUser(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public User update(User newUser,Long userId) {
        User user = users.get(userId);
        String email = newUser.getEmail();
            if (!user.getEmail().equals(email) && emails.contains(email)) {
                throw new EmailAlreadyExistException(
                        String.format("Невозможно поменять email на %s,он уже занят", email)
                );
            }
            user.setName(newUser.getName());
            emails.remove(user.getEmail());
            user.setEmail(email);
            emails.add(email);

        return user;
    }


    @Override
    public void deleteUserById(Long id) {
        if (users.containsKey(id)) {
            User user = users.remove(id);
            emails.remove(user.getEmail());
        }
    }
}
