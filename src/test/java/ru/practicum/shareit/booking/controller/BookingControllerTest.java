package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.EntityNotFoundException;


import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingService bookingService;

    @Autowired
    private MockMvc mvc;

    long bookerId = 5L;
    long itemId = 1L;
    long bookingId = 1L;

    private final LocalDateTime start = LocalDateTime.of(2023, 12, 9, 12, 0);
    private final LocalDateTime end = start.plusDays(3);
    private final BookingDtoRequest request = BookingDtoRequest.builder().start(start)
            .end(end).itemId(1L).build();

    private final BookingDtoResponse response = BookingDtoResponse.builder()
            .id(bookingId)
            .start(start)
            .end(end)
            .booker(new BookingDtoResponse.UserDtoShort(bookerId))
            .item(new BookingDtoResponse.ItemDtoShort(itemId, "Название вещи"))
            .build();


    @Test
    void createBooking() throws Exception {

        when(bookingService.create(request, bookerId))
                .thenReturn(response);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(request))
                        .header("X-Sharer-User-Id", bookerId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(dateTimeFormatter.format(response.getStart()))))
                .andExpect(jsonPath("$.end", is(dateTimeFormatter.format(response.getEnd()))))
                .andExpect(jsonPath("$.booker.id", is(response.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(response.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(response.getItem().getName())));

        Mockito.verify(bookingService, Mockito.times(1))
                .create(request, bookerId);
        Mockito.verifyNoMoreInteractions(bookingService);
    }

    @Test
    void createBookingWhenStartDateIsWrong() throws Exception {
        BookingDtoRequest addBookingDto = BookingDtoRequest.builder().start(null).end(end)
                .itemId(itemId).build();

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(addBookingDto))
                        .header("X-Sharer-User-Id", bookerId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBookingWhenEndDateIsWrong() throws Exception {
        BookingDtoRequest addBookingDto = BookingDtoRequest.builder().start(start).end(null)
                .itemId(itemId).build();

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(addBookingDto))
                        .header("X-Sharer-User-Id", bookerId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void approveBooking() throws Exception {
        long ownerId = 6L;

        when(bookingService.approve(bookingId, true, ownerId))
                .thenReturn(response);

        mvc.perform(patch("/bookings/" + bookingId)
                        .header("X-Sharer-User-Id", ownerId)
                        .param("approved", String.valueOf(true))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(dateTimeFormatter.format(response.getStart()))))
                .andExpect(jsonPath("$.end", is(dateTimeFormatter.format(response.getEnd()))))
                .andExpect(jsonPath("$.booker.id", is(response.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(response.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(response.getItem().getName())));

        Mockito.verify(bookingService, Mockito.times(1))
                .approve(bookingId, true, ownerId);
        Mockito.verifyNoMoreInteractions(bookingService);
    }

    @Test
    void getBooking() throws Exception {
        long userId = 5L;
        when(bookingService.getBooking(bookingId, userId))
                .thenReturn(response);

        mvc.perform(get("/bookings/" + bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(response.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(dateTimeFormatter.format(response.getStart()))))
                .andExpect(jsonPath("$.end", is(dateTimeFormatter.format(response.getEnd()))))
                .andExpect(jsonPath("$.booker.id", is(response.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(response.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.item.name", is(response.getItem().getName())));

        Mockito.verify(bookingService, Mockito.times(1))
                .getBooking(bookingId, userId);
        Mockito.verifyNoMoreInteractions(bookingService);
    }

    @Test
    void getBookingWhenBookingDoesNotExistShouldReturnStatus404() throws Exception {
        long bookingId = 991L;
        long userId = 5L;

        when(bookingService.getBooking(bookingId, userId))
                .thenThrow(new EntityNotFoundException("Бронирование с идентификатором = " + bookingId + " не найдено."));

        mvc.perform(get("/bookings/" + bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        Mockito.verify(bookingService, Mockito.times(1))
                .getBooking(bookingId, userId);
        Mockito.verifyNoMoreInteractions(bookingService);
    }

    @Test
    void findBookingsByBookerId() throws Exception {
        long userId = 5L;
        long itemId1 = 1L;
        long itemId2 = 2L;
        long bookingId1 = 1L;
        long bookingId2 = 2L;
        BookingState state = BookingState.FUTURE;

        LocalDateTime bookingStartDate1 = start.plusDays(1);
        LocalDateTime bookingEndDate1 = end.plusDays(5);
        LocalDateTime bookingStartDate2 = start.plusDays(10);
        LocalDateTime bookingEndDate2 = end.plusDays(15);

        List<BookingDtoResponse> bookings = List.of(
                BookingDtoResponse.builder()
                        .id(bookingId1)
                        .start(bookingStartDate1)
                        .end(bookingEndDate1)
                        .booker(new BookingDtoResponse.UserDtoShort(userId))
                        .item(new BookingDtoResponse.ItemDtoShort(itemId1, "Название вещи 1"))
                        .build(),
                BookingDtoResponse.builder()
                        .id(bookingId2)
                        .start(bookingStartDate2)
                        .end(bookingEndDate2)
                        .booker(new BookingDtoResponse.UserDtoShort(userId))
                        .item(new BookingDtoResponse.ItemDtoShort(itemId2, "Название вещи 2"))
                        .build()
        );

        when(bookingService.findAllByBookerId(state, userId, 0, 10))
                .thenReturn(bookings);

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", String.valueOf(state))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(bookings.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(dateTimeFormatter.format(bookings.get(0).getStart()))))
                .andExpect(jsonPath("$[0].end", is(dateTimeFormatter.format(bookings.get(0).getEnd()))))
                .andExpect(jsonPath("$[0].booker.id", is(bookings.get(0).getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookings.get(0).getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(bookings.get(0).getItem().getName())))
                .andExpect(jsonPath("$[1].id", is(bookings.get(1).getId()), Long.class))
                .andExpect(jsonPath("$[1].start", is(dateTimeFormatter.format(bookings.get(1).getStart()))))
                .andExpect(jsonPath("$[1].end", is(dateTimeFormatter.format(bookings.get(1).getEnd()))))
                .andExpect(jsonPath("$[1].booker.id", is(bookings.get(1).getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[1].item.id", is(bookings.get(1).getItem().getId()), Long.class))
                .andExpect(jsonPath("$[1].item.name", is(bookings.get(1).getItem().getName())));

        Mockito.verify(bookingService, Mockito.times(1))
                .findAllByBookerId(state, userId, 0, 10);
        Mockito.verifyNoMoreInteractions(bookingService);
    }

    @Test
    void findBookingsByOwnerId() throws Exception {
        long ownerId = 4L;
        long userId = 5L;
        long itemId1 = 1L;
        long itemId2 = 1L;
        long bookingId1 = 1L;
        long bookingId2 = 2L;
        BookingState state = BookingState.FUTURE;


        LocalDateTime bookingStartDate1 = start.plusDays(1);
        LocalDateTime bookingEndDate1 = end.plusDays(5);
        LocalDateTime bookingStartDate2 = start.plusDays(10);
        LocalDateTime bookingEndDate2 = end.plusDays(15);

        List<BookingDtoResponse> bookings = List.of(
                BookingDtoResponse.builder()
                        .id(bookingId1)
                        .start(bookingStartDate1)
                        .end(bookingEndDate1)
                        .booker(new BookingDtoResponse.UserDtoShort(userId))
                        .item(new BookingDtoResponse.ItemDtoShort(itemId1, "Название вещи 1"))
                        .build(),
                BookingDtoResponse.builder()
                        .id(bookingId2)
                        .start(bookingStartDate2)
                        .end(bookingEndDate2)
                        .booker(new BookingDtoResponse.UserDtoShort(userId))
                        .item(new BookingDtoResponse.ItemDtoShort(itemId2, "Название вещи 2"))
                        .build()
        );

        when(bookingService.findAllByOwnerId(state, ownerId, 0, 10))
                .thenReturn(bookings);

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", ownerId)
                        .param("state", String.valueOf(state))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(bookings.get(0).getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(dateTimeFormatter.format(bookings.get(0).getStart()))))
                .andExpect(jsonPath("$[0].end", is(dateTimeFormatter.format(bookings.get(0).getEnd()))))
                .andExpect(jsonPath("$[0].booker.id", is(bookings.get(0).getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookings.get(0).getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].item.name", is(bookings.get(0).getItem().getName())))
                .andExpect(jsonPath("$[1].id", is(bookings.get(1).getId()), Long.class))
                .andExpect(jsonPath("$[1].start", is(dateTimeFormatter.format(bookings.get(1).getStart()))))
                .andExpect(jsonPath("$[1].end", is(dateTimeFormatter.format(bookings.get(1).getEnd()))))
                .andExpect(jsonPath("$[1].booker.id", is(bookings.get(1).getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[1].item.id", is(bookings.get(1).getItem().getId()), Long.class))
                .andExpect(jsonPath("$[1].item.name", is(bookings.get(1).getItem().getName())));

        Mockito.verify(bookingService, Mockito.times(1))
                .findAllByOwnerId(state, ownerId, 0, 10);
        Mockito.verifyNoMoreInteractions(bookingService);
    }

    @Test
    void findBookingsByOwnerIdWhenWrongState() throws Exception {
        long ownerId = 4L;
        String state = "Unsupported state";

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", ownerId)
                        .param("state", state)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        Mockito.verifyNoInteractions(bookingService);
    }
}
