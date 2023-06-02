package ru.practicum.shareit.item.dto;

import lombok.*;

import java.util.Collection;

@Data
@Builder
public class ItemDtoResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingDtoShort lastBooking;
    private BookingDtoShort nextBooking;
    private Collection<CommentDtoResponse> comments;

    @AllArgsConstructor
    @Getter
    @Setter
    @ToString
    public static class BookingDtoShort {
        private Long id;
        private Long bookerId;
    }
}
