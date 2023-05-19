package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class ItemStorageImpl implements ItemStorage {

    private final Map<Long, Item> items = new HashMap<>();
    private long counterId = 0;

    @Override
    public Item create(Item item) {
        item.setId(++counterId);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Collection<Item> getAllUserItems(long userId) {
        return items.values()
                .stream().filter(item -> item.getOwnerId() == userId).collect(Collectors.toList());
    }

    @Override
    public Optional<Item> getItem(long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public Item update(Item newItem, long itemId) {
        Item item = items.get(itemId);
        item.setName(newItem.getName());
        item.setDescription(newItem.getDescription());
        item.setAvailable(newItem.getAvailable());
        return item;
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

