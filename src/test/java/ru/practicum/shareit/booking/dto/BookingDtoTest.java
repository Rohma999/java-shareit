package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoTest {

    @Autowired
    JacksonTester<BookingDtoResponse> json;

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Test
    void serializeTest() throws Exception {

        LocalDateTime start = LocalDateTime.parse("2023-06-06T10:00:00", dateTimeFormatter);
        LocalDateTime end = LocalDateTime.parse("2023-06-07T11:00:00", dateTimeFormatter);
        BookingDtoResponse.UserDtoShort userDtoShort = new BookingDtoResponse.UserDtoShort(1L);
        BookingDtoResponse.ItemDtoShort itemDtoShort = new BookingDtoResponse.ItemDtoShort(1L, "Pen");

        BookingDtoResponse dto = BookingDtoResponse.builder()
                .id(1)
                .start(start)
                .end(end)
                .booker(userDtoShort)
                .item(itemDtoShort)
                .build();

        JsonContent<BookingDtoResponse> result = json.write(dto);
        System.out.println(result);
        assertThat(result).hasJsonPath("$.id");
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).hasJsonPath("$.start");
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo(start.format(dateTimeFormatter));
        assertThat(result).hasJsonPath("$.end");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo(end.format(dateTimeFormatter));
        assertThat(result).hasJsonPath("$.booker");
        assertThat(result).extractingJsonPathValue("$.booker").isNotNull();
        assertThat(result).hasJsonPath("$.item");
        assertThat(result).extractingJsonPathValue("$.item").isNotNull();
    }
}