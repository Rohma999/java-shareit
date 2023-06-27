package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.booking.model.BookingStatus;


import java.time.LocalDateTime;

@Data
@Builder
public class BookingDtoResponse {
    private long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private ItemDtoShort item;
    private UserDtoShort booker;
    private BookingStatus status;

    @AllArgsConstructor
    @Getter
    @Setter
    @ToString
    public static class ItemDtoShort {
        private Long id;
        private String name;
    }

    @AllArgsConstructor
    @Getter
    @Setter
    @ToString
    public static class UserDtoShort {
        private Long id;
    }
}
