import model.User;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class UserDAOIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine").withDatabaseName("testdb").withUsername("test").withPassword("test");
    private static SessionFactory sessionFactory;
    private UserDAO userDAO;

    @BeforeAll
    static void setUpDatabase() {
        sessionFactory = new Configuration().configure("hibernate-test.cfg.xml")
                .setProperty("hibernate.connection.url", POSTGRES.getJdbcUrl())
                .setProperty("hibernate.connection.username", POSTGRES.getUsername())
                .setProperty("hibernate.connection.password", POSTGRES.getPassword())
                .setProperty("hibernate.hbm2ddl.auto", "create").buildSessionFactory();
    }

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO(sessionFactory);
    }

    @AfterEach
    void cleanDatabase() {
        try (var session = sessionFactory.openSession()) {
            var transaction = session.beginTransaction();
            session.createNativeQuery("TRUNCATE TABLE \"user\" RESTART IDENTITY CASCADE").executeUpdate();
            transaction.commit();
        }
    }

    @AfterAll
    static void tearDownDatabase() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Test
    void save_shouldPersistUser() {
        User user = createUser("Ivan", "ivan@test.com", 25);
        userDAO.save(user);
        assertNotNull(user.getId());
        User savedUser = userDAO.findUserById(user.getId()).orElseThrow();
        assertNotNull(savedUser);
        assertEquals(user.getId(), savedUser.getId());
        assertEquals("Ivan", savedUser.getName());
        assertEquals("ivan@test.com", savedUser.getEmail());
        assertEquals(25, savedUser.getAge());
        assertNotNull(savedUser.getCreatedAt());
    }

    @Test
    void findUserById_shouldReturnUser_whenUserExists() {
        User user = createUser("Ivan", "ivan@test.com", 25);
        userDAO.save(user);
        Optional<User> result = userDAO.findUserById(user.getId());
        assertTrue(result.isPresent());
        User foundUser = result.get();
        assertEquals(user.getId(), foundUser.getId());
        assertEquals("Ivan", foundUser.getName());
        assertEquals("ivan@test.com", foundUser.getEmail());
        assertEquals(25, foundUser.getAge());
    }

    @Test
    void findUserById_shouldReturnEmpty_whenUserDoesNotExist() {
        Optional<User> result = userDAO.findUserById(999999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByEmail_shouldReturnUser_whenEmailExists() {
        User user = createUser("Ivan", "ivan@test.com", 25);
        userDAO.save(user);
        User result = userDAO.findByEmail("ivan@test.com");
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals("Ivan", result.getName());
        assertEquals("ivan@test.com", result.getEmail());
        assertEquals(25, result.getAge());
    }

    @Test
    void findByEmail_shouldReturnNull_whenEmailDoesNotExist() {
        User result = userDAO.findByEmail("not-found@test.com");
        assertNull(result);
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        User first = createUser("Ivan", "ivan@test.com", 25);
        User second = createUser("Petr", "petr@test.com", 30);
        userDAO.save(first);
        userDAO.save(second);
        List<User> users = userDAO.findAll();
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(user -> user.getEmail().equals("ivan@test.com")));
        assertTrue(users.stream().anyMatch(user -> user.getEmail().equals("petr@test.com")));
    }

    @Test
    void findAll_shouldReturnEmptyList_whenDatabaseIsEmpty() {
        List<User> users = userDAO.findAll();
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void update_shouldUpdateExistingUser() {
        User user = createUser("Ivan", "old@test.com", 25);
        userDAO.save(user);
        user.setName("Petr");
        user.setEmail("new@test.com");
        user.setAge(30);
        userDAO.update(user);
        User updatedUser = userDAO.findUserById(user.getId()).orElseThrow();
        assertEquals(user.getId(), updatedUser.getId());
        assertEquals("Petr", updatedUser.getName());
        assertEquals("new@test.com", updatedUser.getEmail());
        assertEquals(30, updatedUser.getAge());
    }

    @Test
    void delete_shouldDeleteExistingUser() {
        User user = createUser("Ivan", "delete@test.com", 25);
        userDAO.save(user);
        boolean deleted = userDAO.delete(user.getId());
        assertTrue(deleted);
        assertTrue(userDAO.findUserById(user.getId()).isEmpty());
    }

    @Test
    void delete_shouldReturnFalse_whenUserDoesNotExist() {
        boolean deleted = userDAO.delete(999999L);
        assertFalse(deleted);
    }

    private User createUser(String name, String email, int age) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setAge(age);
        return user;
    }
}