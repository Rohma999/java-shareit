package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDtoRequest;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.request.service.ItemRequestService;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.LocalDateTime.now;
import static java.util.List.of;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    private final String headerSharerUserId = "X-Sharer-User-Id";
    private final CommentDtoResponse commentDto = CommentDtoResponse.builder()
            .id(1L)
            .text("qwerty")
            .authorName("Paul")
            .created(now())
            .build();
    private final ItemDtoResponse itemDtoResponse = ItemDtoResponse.builder()
            .id(1L)
            .name("blue pen")
            .description("my blue pen")
            .available(true)
            .comments(of(commentDto))
            .build();
    private final ItemDtoRequest itemDtoRequest = ItemDtoRequest.builder()
            .id(1L)
            .name("blue pen")
            .description("my blue pen")
            .available(true)
            .requestId(1L)
            .build();
    @MockBean
    ItemRequestService itemRequestService;
    @MockBean
    ItemService itemService;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;

    @Test
    void shouldCreateTest() throws Exception {
        when(itemService.create(any(), anyLong()))
                .thenReturn(itemDtoResponse);
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDtoRequest))
                        .header(headerSharerUserId, 1)
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.description", is(itemDtoResponse.getDescription())))
                .andExpect(jsonPath("$.id", is(itemDtoResponse.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoResponse.getName())))
                .andExpect(jsonPath("$.requestId", is(itemDtoResponse.getRequestId()), Long.class))
                .andExpect(status().isOk());
    }


    @Test
    void shouldGetAllItemsTest() throws Exception {
        when(itemService.getAllUserItems(anyLong(), anyInt(), anyInt()))
                .thenReturn(of(itemDtoResponse));
        mvc.perform(get("/items")
                        .header(headerSharerUserId, 1)
                        .param("size", "1")
                        .param("from", "0")
                )
                .andExpect(jsonPath("$[0].description", is(itemDtoResponse.getDescription())))
                .andExpect(jsonPath("$[0].id", is(itemDtoResponse.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDtoResponse.getName())))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetItemTest() throws Exception {
        when(itemService.getItem(anyLong(), anyLong()))
                .thenReturn(itemDtoResponse);
        mvc.perform(get("/items/{itemId}", 1)
                        .header(headerSharerUserId, 1)
                )
                .andExpect(jsonPath("$.description", is(itemDtoResponse.getDescription())))
                .andExpect(jsonPath("$.id", is(itemDtoResponse.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoResponse.getName())))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateTest() throws Exception {
        when(itemService.update(anyLong(), anyLong(), any()))
                .thenReturn(itemDtoResponse);
        mvc.perform(patch("/items/{itemId}", 1)
                        .content(mapper.writeValueAsString(itemDtoRequest))
                        .header(headerSharerUserId, 1)
                        .contentType(APPLICATION_JSON)
                        .accept(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                )
                .andExpect(jsonPath("$.description", is(itemDtoResponse.getDescription())))
                .andExpect(jsonPath("$.id", is(itemDtoResponse.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDtoResponse.getName())))
                .andExpect(status().isOk());
    }

    @Test
    void shouldSaveCommentTest() throws Exception {
        when(itemService.addComment(anyLong(), anyLong(), any(CommentDtoRequest.class)))
                .thenReturn(commentDto);
        mvc.perform(post("/items/{itemId}/comment", 1)
                        .content(mapper.writeValueAsString(commentDto))
                        .header(headerSharerUserId, 1)
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())))
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(status().isOk());
    }

    @Test
    void searchShouldReturnItemsTest() throws Exception {
        when(itemService.search(anyString(), anyInt(), anyInt()))
                .thenReturn(of(itemDtoResponse));
        mvc.perform(get("/items/search")
                        .header(headerSharerUserId, 1)
                        .param("size", "1")
                        .param("from", "0")
                        .param("text", "pen")
                )
                .andExpect(jsonPath("$[0].description", is(itemDtoResponse.getDescription())))
                .andExpect(jsonPath("$[0].id", is(itemDtoResponse.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDtoResponse.getName())))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(status().isOk());
    }

    @Test
    void searchShouldReturnEmptyListTest() throws Exception {
        mvc.perform(get("/items/search")
                        .header(headerSharerUserId, 1)
                        .param("size", "1")
                        .param("from", "0")
                        .param("text", "")
                )
                .andExpect(jsonPath("$", hasSize(0)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldNotUpdateThenThrowNotFoundExceptionTest() throws Exception {
        when(itemService.update(anyLong(), anyLong(), any()))
                .thenThrow(EntityNotFoundException.class);
        mvc.perform(patch("/items/{itemId}", 1)
                        .header(headerSharerUserId, 1)
                        .content(mapper.writeValueAsString(itemDtoRequest))
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotGetItemThenThrowNotFoundExceptionTest() throws Exception {
        when(itemService.getItem(anyLong(), anyLong()))
                .thenThrow(EntityNotFoundException.class);
        mvc.perform(get("/items/{itemId}", 1)
                        .header(headerSharerUserId, 1)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotCreateCommentWithEmptyDtoTest() throws Exception {
        when(itemService.addComment(anyLong(), anyLong(), any(CommentDtoRequest.class)))
                .thenThrow(ValidationException.class);
        mvc.perform(post("/items/{itemId}/comment", 1)
                        .header(headerSharerUserId, 1)
                        .content(mapper.writeValueAsString(CommentDtoRequest.builder().build()))
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldNotCreateWithEmptyDtoTest() throws Exception {
        mvc.perform(post("/items", 1)
                        .header(headerSharerUserId, 1)
                        .content(mapper.writeValueAsString(ItemDtoRequest.builder().build()))
                        .contentType(APPLICATION_JSON)
                        .characterEncoding(UTF_8)
                        .accept(APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }
}