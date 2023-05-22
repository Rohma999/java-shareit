package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Collection;


public interface ItemService {
    ItemDto create(ItemDto itemDto,long userId);

    Collection<ItemDto> getAllUserItems(long userId);

    ItemDto getItem(long itemId);

    ItemDto update(long userId,long itemId,ItemDto itemDto);

    Collection<ItemDto> search(String text);


}
