import exception.EmailAlreadyExistsException;
import exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private UserService userService;

    @Test
    void create_shouldSaveUser_whenEmailIsUnique() {
        User user = createUser(1L, "Ivan", "ivan@test.com", 25);
        when(userDAO.findByEmail("ivan@test.com")).thenReturn(null);
        userService.create(user);
        verify(userDAO).findByEmail("ivan@test.com");
        verify(userDAO).save(user);
    }

    @Test
    void create_shouldThrowException_whenEmailAlreadyExists() {
        User user = createUser(1L, "Ivan", "ivan@test.com", 25);
        User existingUser = createUser(2L, "Petr", "ivan@test.com", 30);
        when(userDAO.findByEmail("ivan@test.com")).thenReturn(existingUser);
        assertThrows(EmailAlreadyExistsException.class, () -> userService.create(user));
        verify(userDAO).findByEmail("ivan@test.com");
        verify(userDAO, never()).save(any());
    }

    @Test
    void update_shouldUpdateUser_whenEmailIsUnique() {
        User user = createUser(1L, "Ivan", "ivan@test.com", 25);
        when(userDAO.findByEmail("ivan@test.com")).thenReturn(null);
        userService.update(user);
        verify(userDAO).findByEmail("ivan@test.com");
        verify(userDAO).update(user);
    }

    @Test
    void update_shouldUpdateUser_whenEmailBelongsToSameUser() {
        User oldUser = createUser(1L, "Ivan", "ivan@test.com", 25);
        User user = createUser(1L, "John", "ivan@test.com", 25);
        when(userDAO.findByEmail("ivan@test.com")).thenReturn(oldUser);
        userService.update(user);
        verify(userDAO).findByEmail("ivan@test.com");
        verify(userDAO).update(user);
    }

    @Test
    void update_shouldThrowException_whenEmailBelongsToAnotherUser() {
        User oldUser = createUser(1L, "Ivan", "ivan@test.com", 25);
        User newUser = createUser(2L, "Petr", "ivan@test.com", 30);
        when(userDAO.findByEmail("ivan@test.com")).thenReturn(oldUser);
        assertThrows(EmailAlreadyExistsException.class, () -> userService.create(newUser));
        verify(userDAO).findByEmail("ivan@test.com");
        verify(userDAO, never()).update(any());
    }

    @Test
    void delete_shouldDeleteUser_whenUserExists() {
        long userId = 1;
        when(userDAO.delete(userId)).thenReturn(true);
        assertDoesNotThrow(() -> userService.delete(userId));
        verify(userDAO).delete(userId);
    }

    @Test
    void delete_shouldThrowException_whenUserDoesNotExist() {
        long userId = 99;
        when(userDAO.delete(userId)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> userService.delete(userId));
        verify(userDAO).delete(userId);
    }

    @Test
    void getById_shouldReturnUser_whenUserExists() {
        User user = createUser(1L, "Ivan", "ivan@test.com", 25);
        when(userDAO.findUserById(1L)).thenReturn(Optional.of(user));
        User result = userService.getById(1L);
        assertNotNull(result);
        assertEquals(user, result);
        verify(userDAO).findUserById(1L);
    }

    @Test
    void getById_shouldThrowException_whenUserDoesNotExist() {
        when(userDAO.findUserById(999L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> userService.getById(999L));
        verify(userDAO).findUserById(999L);
    }

    @Test
    void getAll_shouldReturnAllUsers() {
        User first = createUser(1L, "Ivan", "ivan@test.com", 25);
        User second = createUser(2L, "Petr", "petr@test.com", 30);
        List<User> users = List.of(first, second);
        when(userDAO.findAll()).thenReturn(users);
        List<User> result = userService.getAll();
        assertEquals(2, result.size());
        assertEquals(users, result);
        verify(userDAO).findAll();
    }

    @Test
    void getAll_shouldReturnEmptyList_whenThereAreNoUsers() {
        when(userDAO.findAll()).thenReturn(List.of());
        List<User> result = userService.getAll();
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userDAO).findAll();
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