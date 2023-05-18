package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotOwnerException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ItemStorageImpl implements ItemStorage {

    private final Map<Long, Item> items = new HashMap<>();
    private long id = 1;

    @Override
    public Item create(Item item) {
        item.setId(id++);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Collection<Item> getAllUserItems(long userId) {
        return items.values()
                .stream().filter(item -> item.getOwnerId() == userId).collect(Collectors.toList());
    }

    @Override
    public Optional<Item> findById(long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public Optional<Item> update(long userId, long itemId, Item item) {
        Item itemRep = items.get(itemId);
        if (itemRep != null) {
            if (itemRep.getOwnerId() != userId) {
                throw new NotOwnerException("Вы не являетесь владельцем данной вещи.");
            }
            String name = item.getName();
            String description = item.getDescription();
            Boolean available = item.getAvailable();
            if (name != null) {
                itemRep.setName(name);
            }
            if (description != null) {
                itemRep.setDescription(description);
            }
            if (available != null) {
                itemRep.setAvailable(available);
            }
            return Optional.of(items.get(itemId));
        }
        return Optional.empty();
    }

    @Override
    public Collection<Item> search(String text) {
        String searchingText = text.toLowerCase();
        return items.values().stream()
                .filter(item -> item.getAvailable() &&
                        (
                                item.getName().toLowerCase().contains(searchingText) ||
                                        item.getDescription().toLowerCase().contains(searchingText)
                        )
                ).sorted(Comparator.comparing(Item::getId)).collect(Collectors.toList());
    }
}

