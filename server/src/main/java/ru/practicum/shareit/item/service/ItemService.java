package ru.practicum.shareit.item.service;


import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoRequest;

import java.util.List;


public interface ItemService {
    ItemDtoResponse create(ItemDtoRequest itemDto, long userId);

    List<ItemDtoResponse> getAllUserItems(long userId, int from, int size);

    ItemDtoResponse getItem(long itemId, long userId);

    ItemDtoResponse update(long userId, long itemId, ItemDtoRequest itemDtoRequest);

    List<ItemDtoResponse> search(String text, int from, int size);

    CommentDtoResponse addComment(long userId, long itemId, CommentDtoRequest commentDtoRequest);
}
