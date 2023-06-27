package ru.practicum.shareit.request.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.RequestItemRequestDto;
import ru.practicum.shareit.request.dto.ResponseItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@Service
@Transactional
public class ItemRequestServiceImpl implements ItemRequestService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Autowired
    public ItemRequestServiceImpl(UserRepository userRepository, ItemRepository itemRepository,
                                  ItemRequestRepository itemRequestRepository) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.itemRequestRepository = itemRequestRepository;
    }

    @Override
    public ResponseItemRequestDto create(RequestItemRequestDto itemRequestDto, long userId) {
        User requester = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователь с id %d не существует", userId)));
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto, requester);
        log.info("Сохраняем в БД запрос на вещь : {}", itemRequest);
        ItemRequest savedItemRequest = itemRequestRepository.save(itemRequest);
        log.info("Возвращаем в контроллер сохраненный запрос на вещь : {}", savedItemRequest);
        return ItemRequestMapper.itemRequestToResponseItemRequestDto(savedItemRequest, new ArrayList<>());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResponseItemRequestDto> findAllByUserId(long userId) {
        userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователь с id %d не существует", userId)));
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterId(userId, Sort.by(DESC, "created"));
        Map<Long, List<Item>> items = getItemsByItemRequests(itemRequests);
        List<ResponseItemRequestDto> response = ItemRequestMapper.itemRequestsToResponseItemRequestDto(itemRequests, items);
        log.info("Передаем в контроллер список запросов пользователя с id {} :{}", userId, response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResponseItemRequestDto> findAll(long userId, int from, int size) {
        userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователь с id %d не существует", userId)));
        PageRequest page = PageRequest.of(from / size, size,Sort.by(DESC, "created"));
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterIdIsNot(userId, page);
        Map<Long, List<Item>> items = getItemsByItemRequests(itemRequests);
        List<ResponseItemRequestDto> response = ItemRequestMapper.itemRequestsToResponseItemRequestDto(itemRequests, items);
        log.info("Передаем в контроллер список запросов {}", response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseItemRequestDto getById(long requestId, long userId) {
        userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователь с id %d не существует", userId)));
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Запрос с id %d не существует", requestId)));

        List<Item> items = itemRepository.findAllByRequestInOrderByRequestId(List.of(itemRequest));
        ResponseItemRequestDto response = ItemRequestMapper.itemRequestToResponseItemRequestDto(itemRequest, items);
        log.info("Передаем в контроллер  запрос {}", response);
        return response;
    }

    private Map<Long, List<Item>> getItemsByItemRequests(List<ItemRequest> itemRequests) {
        return itemRepository.findAllByRequestInOrderByRequestId(itemRequests)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId(), Collectors.toList()));
    }
}
