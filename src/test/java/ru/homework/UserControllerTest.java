package ru.homework;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.homework.controller.UserController;
import ru.homework.dto.UserRequest;
import ru.homework.dto.UserResponse;
import ru.homework.exception.EmailAlreadyExistsException;
import ru.homework.exception.NotFoundException;
import ru.homework.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserRequest createRequest() {
        UserRequest request = new UserRequest();
        request.setName("Ivan");
        request.setEmail("ivan@test.com");
        request.setAge(25);
        return request;
    }

    private UserResponse createResponse() {
        return new UserResponse(1L, "Ivan", "ivan@test.com", 25, LocalDateTime.of(2026, 1, 1, 12, 0));
    }

    @Test
    void create_shouldReturn201_whenRequestIsValid() throws Exception {
        UserRequest request = createRequest();
        UserResponse response = createResponse();

        when(userService.create(any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@test.com"))
                .andExpect(jsonPath("$.age").value(25));

        verify(userService).create(any(UserRequest.class));
    }

    @Test
    void create_shouldReturn400_whenNameIsBlank() throws Exception {
        UserRequest request = createRequest();
        request.setName(" ");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn400_whenEmailIsInvalid() throws Exception {
        UserRequest request = createRequest();
        request.setEmail("email");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturn409_whenEmailAlreadyExists() throws Exception {
        UserRequest request = createRequest();

        when(userService.create(any(UserRequest.class))).thenThrow(new EmailAlreadyExistsException("Пользователь с таким email уже существует."));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void update_shouldReturn200_whenUserExists() throws Exception {
        UserRequest request = createRequest();
        UserResponse response = createResponse();

        when(userService.update(eq(1L), any(UserRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ivan"));

        verify(userService).update(eq(1L), any(UserRequest.class));
    }

    @Test
    void update_shouldReturn404_whenUserDoesNotExist() throws Exception {
        UserRequest request = createRequest();

        when(userService.update(eq(999L), any(UserRequest.class))).thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(put("/api/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldReturn204_whenUserExists() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete("/api/users/1")).andExpect(status().isNoContent());

        verify(userService).delete(1L);
    }

    @Test
    void delete_shouldReturn404_whenUserDoesNotExist() throws Exception {
        doThrow(new NotFoundException("Пользователь не найден")).when(userService).delete(999L);

        mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_shouldReturn200_whenUserExists() throws Exception {
        when(userService.getById(1L)).thenReturn(createResponse());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ivan"))
                .andExpect(jsonPath("$.email").value("ivan@test.com"));
    }

    @Test
    void getById_shouldReturn404_whenUserDoesNotExist() throws Exception {
        when(userService.getById(999L)).thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAll_shouldReturn200AndListOfUsers() throws Exception {
        when(userService.getAll()).thenReturn(List.of(createResponse()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Ivan"));
    }

    @Test
    void getAll_shouldReturnEmptyList_whenNoUsersExist() throws Exception {
        when(userService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
