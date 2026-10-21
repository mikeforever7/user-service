package ru.homework;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import ru.homework.dto.UserRequest;
import ru.homework.dto.UserResponse;
import ru.homework.exception.EmailAlreadyExistsException;
import ru.homework.exception.NotFoundException;
import ru.homework.model.User;
import ru.homework.repository.UserRepository;
import ru.homework.service.UserServiceImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void create_shouldSaveUser_whenEmailIsUnique() {
        UserRequest request = createRequest("Ivan", "ivan@test.com", 25);
        when(userRepository.findByEmail("ivan@test.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);
                    user.setId(1L);
                    return user;
                });
        UserResponse result = userService.create(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Ivan", result.getName());
        assertEquals("ivan@test.com", result.getEmail());
        assertEquals(25, result.getAge());

        verify(userRepository).findByEmail("ivan@test.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_shouldThrowException_whenEmailAlreadyExists() {
        UserRequest request = createRequest("Ivan", "ivan@test.com", 25);
        User existingUser = createUser(2L, "Petr", "ivan@test.com", 30);

        when(userRepository.findByEmail("ivan@test.com")).thenReturn(Optional.of(existingUser));

        assertThrows(EmailAlreadyExistsException.class, () -> userService.create(request));

        verify(userRepository).findByEmail("ivan@test.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_shouldUpdateUser_whenEmailIsUnique() {
        UserRequest request = createRequest("Ivan Updated", "ivan@test.com", 26);
        User existingUser = createUser(1L, "Ivan", "ivan@test.com", 25);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.findByEmail("ivan@test.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponse result = userService.update(1L, request);

        assertEquals(1L, result.getId());
        assertEquals("Ivan Updated", result.getName());
        assertEquals("ivan@test.com", result.getEmail());
        assertEquals(26, result.getAge());

        verify(userRepository).findById(1L);
        verify(userRepository).findByEmail("ivan@test.com");
        verify(userRepository).save(existingUser);
    }

    @Test
    void update_shouldThrowException_whenUserDoesNotExist() {
        UserRequest request = createRequest("Ivan", "ivan@test.com", 25);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.update(99L, request));

        verify(userRepository).findById(99L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void update_shouldThrowException_whenEmailBelongsToAnotherUser() {
        UserRequest request = createRequest("Petr", "ivan@test.com", 30);
        User existingUser = createUser(1L, "Ivan", "ivan@test.com", 25);
        User anotherUser = createUser(2L, "Anna", "anna@test.com", 28);

        when(userRepository.findById(2L)).thenReturn(Optional.of(anotherUser));
        when(userRepository.findByEmail("ivan@test.com")).thenReturn(Optional.of(existingUser));

        assertThrows(EmailAlreadyExistsException.class, () -> userService.update(2L, request));

        verify(userRepository).findById(2L);
        verify(userRepository).findByEmail("ivan@test.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void delete_shouldDeleteUser_whenUserExists() {
        User user = createUser(1L, "Ivan", "ivan@test.com", 25);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> userService.delete(1L));

        verify(userRepository).findById(1L);
        verify(userRepository).delete(user);
    }

    @Test
    void delete_shouldThrowException_whenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.delete(99L));

        verify(userRepository).findById(99L);
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void getById_shouldReturnUser_whenUserExists() {
        User user = createUser(1L, "Ivan", "ivan@test.com", 25);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse result = userService.getById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Ivan", result.getName());
        assertEquals("ivan@test.com", result.getEmail());
        assertEquals(25, result.getAge());

        verify(userRepository).findById(1L);
    }

    @Test
    void getById_shouldThrowException_whenUserDoesNotExist() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getById(999L));

        verify(userRepository).findById(999L);
    }

    @Test
    void getAll_shouldReturnAllUsers() {
        User first = createUser(1L, "Ivan", "ivan@test.com", 25);
        User second = createUser(2L, "Petr", "petr@test.com", 30);

        when(userRepository.findAll()).thenReturn(List.of(first, second));

        List<UserResponse> result = userService.getAll();

        assertEquals(2, result.size());
        assertEquals("Ivan", result.get(0).getName());
        assertEquals("Petr", result.get(1).getName());

        verify(userRepository).findAll();
    }

    @Test
    void getAll_shouldReturnEmptyList_whenThereAreNoUsers() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponse> result = userService.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(userRepository).findAll();
    }

    private UserRequest createRequest(String name, String email, int age) {
        UserRequest request = new UserRequest();
        request.setName(name);
        request.setEmail(email);
        request.setAge(age);
        return request;
    }

    private User createUser(Long id, String name, String email, int age) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setAge(age);
        return user;
    }
}