package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Optional;

public interface ItemStorage {

    Item create(Item item);

    Collection<Item> getAllUserItems(long userId);

    Optional<Item> findById(long itemId);

    Optional<Item> update(long userId, long itemId, Item item);

    Collection<Item> search(String text);
}
