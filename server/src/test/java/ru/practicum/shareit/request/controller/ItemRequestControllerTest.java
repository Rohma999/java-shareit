package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.EntityNotFoundException;
import ru.practicum.shareit.request.dto.RequestItemRequestDto;
import ru.practicum.shareit.request.dto.ResponseItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.List;

import org.mockito.Mockito;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemRequestService itemRequestService;

    @Autowired
    private MockMvc mvc;
    private final LocalDateTime created = LocalDateTime.of(2023, 9, 12, 7, 9);

    private final RequestItemRequestDto request = RequestItemRequestDto.builder()
            .description("Описание запроса").build();

    private final ResponseItemRequestDto response = ResponseItemRequestDto.builder()
            .id(1L)
            .description("Описание запроса")
            .created(created)
            .items(List.of())
            .build();

    @Test
    void createItemRequest() throws Exception {
        long userId = 1;
        when(itemRequestService.create(request, 1))
                .thenReturn(response);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(request))
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(response.getDescription())))
                .andExpect(jsonPath("$.created").value(dateTimeFormatter.format(created)))
                .andExpect(jsonPath("$.items", is(response.getItems())));

        Mockito.verify(itemRequestService, Mockito.times(1))
                .create(request, userId);
        Mockito.verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    void createItemRequest_whenWithoutUserId_status400() throws Exception {


        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(request))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(in(List.of(400, 500))));

        Mockito.verifyNoInteractions(itemRequestService);
    }

    @Test
    void getItemRequestByRequester() throws Exception {
        LocalDateTime created1 = created.plusDays(1);
        Long userId = 1L;

        List<ResponseItemRequestDto> requestDtos = List.of(
                ResponseItemRequestDto.builder()
                        .id(1L)
                        .description("Запрос 1")
                        .created(created)
                        .items(List.of())
                        .build(),
                ResponseItemRequestDto.builder()
                        .id(2L)
                        .description("Запрос 2")
                        .created(created1)
                        .items(List.of())
                        .build()
        );

        when(itemRequestService.findAllByUserId(userId))
                .thenReturn(requestDtos);

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(requestDtos.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestDtos.get(0).getDescription())))
                .andExpect(jsonPath("$[0].created").value(dateTimeFormatter.format(created)))
                .andExpect(jsonPath("$[0].items", is(requestDtos.get(0).getItems())))
                .andExpect(jsonPath("$[1].id", is(requestDtos.get(1).getId()), Long.class))
                .andExpect(jsonPath("$[1].description", is(requestDtos.get(1).getDescription())))
                .andExpect(jsonPath("$[1].created").value(dateTimeFormatter.format(created1)))
                .andExpect(jsonPath("$[1].items", is(requestDtos.get(0).getItems())));

        Mockito.verify(itemRequestService, Mockito.times(1))
                .findAllByUserId(userId);
        Mockito.verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    void getAllItemRequest() throws Exception {
        LocalDateTime created1 = created.plusDays(1);
        Long userId = 1L;
        int from = 0;
        int size = 10;

        List<ResponseItemRequestDto> requestDtos = List.of(
                ResponseItemRequestDto.builder()
                        .id(1L)
                        .description("Запрос 1")
                        .created(created)
                        .items(List.of())
                        .build(),
                ResponseItemRequestDto.builder()
                        .id(2L)
                        .description("Запрос 2")
                        .created(created1)
                        .items(List.of())
                        .build()
        );

        when(itemRequestService.findAll(userId, from, size)).thenReturn(requestDtos);

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(requestDtos.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestDtos.get(0).getDescription())))
                .andExpect(jsonPath("$[0].created").value(dateTimeFormatter.format(created)))
                .andExpect(jsonPath("$[0].items", is(requestDtos.get(0).getItems())))
                .andExpect(jsonPath("$[1].id", is(requestDtos.get(1).getId()), Long.class))
                .andExpect(jsonPath("$[1].description", is(requestDtos.get(1).getDescription())))
                .andExpect(jsonPath("$[1].created").value(dateTimeFormatter.format(created1)))
                .andExpect(jsonPath("$[1].items", is(requestDtos.get(0).getItems())));

        Mockito.verify(itemRequestService, Mockito.times(1)).findAll(userId, from, size);
        Mockito.verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    void getItemRequest() throws Exception {
        Long requestId = 1L;
        Long userId = 1L;
        when(itemRequestService.getById(requestId, userId))
                .thenReturn(response);

        mvc.perform(get("/requests/" + requestId)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(response.getDescription())))
                .andExpect(jsonPath("$.created").value(dateTimeFormatter.format(created)))
                .andExpect(jsonPath("$.items", is(response.getItems())));

        Mockito.verify(itemRequestService, Mockito.times(1))
                .getById(requestId, userId);
        Mockito.verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    void getItemRequestWhenNotExistRequestIdThenStatus404() throws Exception {
        Long requestId = 99L;
        Long userId = 1L;
        when(itemRequestService.getById(requestId, userId))
                .thenThrow(new EntityNotFoundException("Запрос с идентификатором " + requestId + " не найден."));

        mvc.perform(get("/requests/" + requestId)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(itemRequestService, Mockito.times(1))
                .getById(requestId, userId);
        Mockito.verifyNoMoreInteractions(itemRequestService);
    }

    @Test
    void getItemRequest_whenNotExistRequesterId_thenStatus404() throws Exception {
        Long requestId = 1L;
        Long userId = 99L;
        when(itemRequestService.getById(requestId, userId))
                .thenThrow(new EntityNotFoundException("Пользователь с идентификатором = " + userId + " не найден."));

        mvc.perform(get("/requests/" + requestId)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(itemRequestService, Mockito.times(1))
                .getById(requestId, userId);
        Mockito.verifyNoMoreInteractions(itemRequestService);
    }
}