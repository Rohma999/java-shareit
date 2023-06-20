package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoTest {
    @Autowired
    private JacksonTester<ItemDtoResponse> itemDtoResponseJacksonTester;
    @Autowired
    private JacksonTester<CommentDtoResponse> commentDtoResponseJacksonTester;
    @Autowired
    private JacksonTester<ItemDtoRequest> itemDtoRequestJacksonTester;

    @Test
    void itemDtoTest() throws Exception {
        ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder()
                .id(1L)
                .name("Pen")
                .description("Blue pen")
                .available(true)
                .requestId(1L)
                .build();

        JsonContent<ItemDtoRequest> jsonContent = itemDtoRequestJacksonTester.write(itemDtoRequest);
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.description")
                .isEqualTo(itemDtoRequest.getDescription());
        assertThat(jsonContent)
                .extractingJsonPathBooleanValue("$.available")
                .isEqualTo(itemDtoRequest.getAvailable());
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.requestId")
                .isEqualTo(itemDtoRequest.getRequestId().intValue());
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.name")
                .isEqualTo(itemDtoRequest.getName());
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.id")
                .isEqualTo(itemDtoRequest.getId().intValue());
    }

    @Test
    void itemAllFieldsDtoTest() throws Exception {

        ItemDtoResponse itemDtoResponse = ItemDtoResponse.builder()
                .id(1L).name("Pen")
                .description("Blue Pen")
                .available(true)
                .lastBooking(new ItemDtoResponse.BookingDtoShort(1L, 1L))
                .build();
        JsonContent<ItemDtoResponse> jsonContent = itemDtoResponseJacksonTester.write(itemDtoResponse);
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.lastBooking.bookerId")
                .isEqualTo(itemDtoResponse.getLastBooking().getBookerId().intValue());
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.lastBooking.id")
                .isEqualTo(itemDtoResponse.getLastBooking().getId().intValue());
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.description")
                .isEqualTo(itemDtoResponse.getDescription());
        assertThat(jsonContent)
                .extractingJsonPathBooleanValue("$.available")
                .isEqualTo(itemDtoResponse.getAvailable());
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.name")
                .isEqualTo(itemDtoResponse.getName());
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.id")
                .isEqualTo(itemDtoResponse.getId().intValue());
        assertThat(jsonContent)
                .extractingJsonPathArrayValue("$.comments")
                .isNullOrEmpty();
        assertThat(jsonContent)
                .extractingJsonPathMapValue("$.lastBooking")
                .isNotNull();
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.requestId")
                .isNull();
        assertThat(jsonContent)
                .extractingJsonPathValue("$.nextBooking")
                .isNull();
    }

    @Test
    void commentDtoTest() throws Exception {
        CommentDtoResponse commentDto = CommentDtoResponse.builder()
                .id(1L)
                .text("My comment")
                .authorName("Norris")
                .created(now())
                .build();
        JsonContent<CommentDtoResponse> jsonContent = commentDtoResponseJacksonTester.write(commentDto);
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.authorName")
                .isEqualTo(commentDto.getAuthorName());
        assertThat(jsonContent)
                .extractingJsonPathStringValue("$.text")
                .isEqualTo(commentDto.getText());
        assertThat(jsonContent)
                .extractingJsonPathNumberValue("$.id")
                .isEqualTo(commentDto.getId().intValue());
    }
}