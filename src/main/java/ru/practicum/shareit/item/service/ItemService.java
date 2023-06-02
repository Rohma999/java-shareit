package ru.practicum.shareit.item.service;


import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoRequest;


import java.util.Collection;


public interface ItemService {
    ItemDtoResponse create(ItemDtoRequest itemDto, long userId);

    Collection<ItemDtoResponse> getAllUserItems(long userId);

    ItemDtoResponse getItem(long itemId,long userId);

    ItemDtoResponse update(long userId,long itemId,ItemDtoRequest itemDtoRequest);

    Collection<ItemDtoResponse> search(String text);

    CommentDtoResponse addComment(long userId, long itemId, CommentDtoRequest commentDtoRequest);
}
