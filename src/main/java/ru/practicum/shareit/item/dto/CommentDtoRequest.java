package ru.practicum.shareit.item.dto;


import lombok.Getter;
import ru.practicum.shareit.group.Create;
import javax.validation.constraints.NotBlank;

@Getter
public class CommentDtoRequest {
    @NotBlank(groups = Create.class)
    private String text;
}
