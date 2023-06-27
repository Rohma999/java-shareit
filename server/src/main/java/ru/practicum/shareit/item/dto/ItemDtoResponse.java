package ru.practicum.shareit.item.dto;

import lombok.*;


import java.util.List;

@Data
@Builder
public class ItemDtoResponse {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
    private BookingDtoShort lastBooking;
    private BookingDtoShort nextBooking;
    private List<CommentDtoResponse> comments;

    @AllArgsConstructor
    @Getter
    @Setter
    @ToString
    public static class BookingDtoShort {
        private Long id;
        private Long bookerId;
    }
}
