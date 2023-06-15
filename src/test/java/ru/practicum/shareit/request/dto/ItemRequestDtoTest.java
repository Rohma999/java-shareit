package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoTest {

    @Autowired
    JacksonTester<ResponseItemRequestDto> json;

    @Test
    void testSerialize() throws Exception {
        long bookingId = 1;
        long itemId = 1;
        long ownerId = 2;
        long ownerId2 = 3;
        long requesterId = 4;
        long itemRequestId = 1;

        UserDto ownerDto = UserDto.builder()
                .id(ownerId)
                .name("Daron Malakian")
                .email("daronmalakian@yandex.ru")
                .build();
        User owner = UserMapper.toUser(ownerDto);

        UserDto ownerDto2 = UserDto.builder()
                .id(ownerId2)
                .name("John Dolmayan")
                .email("johndolmayan@mail.com")
                .build();
        User owner2 = UserMapper.toUser(ownerDto2);

        UserDto requesterDto = UserDto.builder()
                .id(requesterId)
                .name("Serj Tankian")
                .email("serjtankian@mail.com")
                .build();
        User requester = UserMapper.toUser(requesterDto);

        RequestItemRequestDto addItemRequestDto = RequestItemRequestDto.builder().description("Описание запроса 1").build();
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(addItemRequestDto, requester);
        itemRequest.setId(itemRequestId);

        ItemDtoRequest itemDto = ItemDtoRequest.builder()
                .id(itemId)
                .name("Вещь 1")
                .description("Описание вещи 1")
                .available(true)
                .build();
        Item item = ItemMapper.toItem(itemDto, owner, itemRequest);

        ItemDtoRequest itemDto2 = ItemDtoRequest.builder()
                .id(itemId)
                .name("Вещь 2")
                .description("Описание вещи 2")
                .available(true)
                .build();
        Item item2 = ItemMapper.toItem(itemDto2, owner2, itemRequest);

        ResponseItemRequestDto dto = ItemRequestMapper.itemRequestToResponseItemRequestDto(itemRequest, List.of(item, item2));

        JsonContent<ResponseItemRequestDto> result = json.write(dto);
        System.out.println(result);
        assertThat(result).hasJsonPath("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo((int) bookingId);
        assertThat(result).hasJsonPath("$.description");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(itemRequest.getDescription());
        assertThat(result).hasJsonPath("$.created");
        assertThat(result).hasJsonPath("$.items[0].id");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo((int) itemId);
        assertThat(result).hasJsonPath("$.items[0].name");
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo(item.getName());
        assertThat(result).hasJsonPath("$.items[0].description");
        assertThat(result).extractingJsonPathStringValue("$.items[0].description").isEqualTo(item.getDescription());
        assertThat(result).hasJsonPath("$.items[0].available");
        assertThat(result).extractingJsonPathBooleanValue("$.items[0].available").isEqualTo(item.getAvailable());
        assertThat(result).hasJsonPath("$.items[1].id");
        assertThat(result).extractingJsonPathNumberValue("$.items[1].id").isEqualTo((int) itemId);
        assertThat(result).hasJsonPath("$.items[1].name");
        assertThat(result).extractingJsonPathStringValue("$.items[1].name").isEqualTo(item2.getName());
        assertThat(result).hasJsonPath("$.items[1].description");
        assertThat(result).extractingJsonPathStringValue("$.items[1].description").isEqualTo(item2.getDescription());
        assertThat(result).hasJsonPath("$.items[1].available");
        assertThat(result).extractingJsonPathBooleanValue("$.items[1].available").isEqualTo(item2.getAvailable());
    }
}