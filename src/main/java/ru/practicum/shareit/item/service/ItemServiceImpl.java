package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    @Autowired
    public ItemServiceImpl(ItemStorage itemStorage, UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    @Override
    public ItemDto create(ItemDto itemDto, long userId) {
        check(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwnerId(userId);
        ItemDto responseDto = ItemMapper.toItemDto(itemStorage.create(item));
        log.info("Передаем в контроллер созданную вещь : {}", itemDto);
        return responseDto;
    }

    @Override
    public Collection<ItemDto> getAllUserItems(long userId) {
        check(userId);
        Collection<ItemDto> items = itemStorage.getAllUserItems(userId).stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
        log.info("Передаем в контроллер список вещей пользователя с id {} : {}", userId, items);
        return items;
    }

    @Override
    public ItemDto findById(long itemId) {
        Item item = itemStorage.findById(itemId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Вещь с id %d не существует", itemId))
        );
        ItemDto responseDto = ItemMapper.toItemDto(item);
        log.info("Передаем в контроллер вещь  с id {} : {}", itemId, responseDto);
        return responseDto;
    }

    @Override
    public ItemDto update(long userId, long itemId, ItemDto itemDto) {
        check(userId);
        Item item = itemStorage.update(userId, itemId, ItemMapper.toItem(itemDto)).orElseThrow(
                () -> new EntityNotFoundException(String.format("Вещь с id %d не существует", itemId))
        );
        ItemDto responseDto = ItemMapper.toItemDto(item);
        log.info("Передаем в контроллер обновленную вещь с id {} : {}", itemId, responseDto);
        return responseDto;
    }

    @Override
    public Collection<ItemDto> search(String text) {
        Collection<ItemDto> items = itemStorage.search(text).stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
        log.info("Передаем в контроллер найденные вещи : {} ", items);
        return items;
    }

    private void check(long userId) {
        userStorage.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Пользователь с id %d не найден", userId))
        );
    }
}
