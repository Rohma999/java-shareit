package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.RequestItemRequestDto;
import ru.practicum.shareit.request.dto.ResponseItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ResponseItemRequestDto create(RequestItemRequestDto itemRequestDto, long userId);

    List<ResponseItemRequestDto> findAllByUserId(long userId);

    List<ResponseItemRequestDto> findAll(long userId, int from, int size);

    ResponseItemRequestDto getById(long itemId, long userId);
}
