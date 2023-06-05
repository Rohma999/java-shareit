package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;


import java.time.LocalDateTime;

public class CommentMapper {

    public static Comment toComment(CommentDtoRequest commentDtoRequest, User author, Item item, LocalDateTime now) {
        return Comment.builder()
                .text(commentDtoRequest.getText())
                .author(author)
                .item(item)
                .created(now)
                .build();
    }

    public static CommentDtoResponse toCommentDtoResponse(Comment comment) {
        return CommentDtoResponse.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }
}
