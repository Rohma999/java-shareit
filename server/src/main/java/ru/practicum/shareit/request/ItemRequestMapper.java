package ru.practicum.shareit.request;

import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.RequestItemRequestDto;
import ru.practicum.shareit.request.dto.ResponseItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequest toItemRequest(RequestItemRequestDto itemRequestDto, User requester) {
        return ItemRequest.builder()
                .description(itemRequestDto.getDescription())
                .created(LocalDateTime.now())
                .requester(requester)
                .build();
    }

    public static ResponseItemRequestDto itemRequestToResponseItemRequestDto(ItemRequest itemRequest, List<Item> items) {
        return ResponseItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .items(ItemMapper.itemsToDto(items))
                .build();
    }

    public static List<ResponseItemRequestDto> itemRequestsToResponseItemRequestDto(
            List<ItemRequest> itemRequests, Map<Long, List<Item>> items) {
        return itemRequests
                .stream()
                .map(ir -> itemRequestToResponseItemRequestDto(ir,
                        items.getOrDefault(ir.getId(), List.of())))
                .collect(Collectors.toList());
    }
}
