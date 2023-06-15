package ru.practicum.shareit.item.dto;


import lombok.*;
import ru.practicum.shareit.group.Create;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDtoRequest {
    @NotBlank(groups = Create.class)
    private String text;
}
