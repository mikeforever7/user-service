import exception.EmailAlreadyExistsException;
import exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import model.User;

import java.util.List;

@Slf4j
public class UserService {
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void create(User user) {
        log.info("Создание пользователя: email={}", user.getEmail());
        if (userDAO.findByEmail(user.getEmail()) != null) {
            log.warn("Создание пользователя отклонено: email={} уже существует", user.getEmail());
            throw new EmailAlreadyExistsException("Пользователь с таким email уже существует.");
        }
        userDAO.save(user);
        log.info("Пользователь успешно создан: id={}, email={}", user.getId(), user.getEmail());
    }

    public void update(User user) {
        log.info("Обновление пользователя: id={}, email={}", user.getId(), user.getEmail());
        User oldUser = userDAO.findByEmail(user.getEmail());
        if (oldUser != null && !oldUser.getId().equals(user.getId())) {
            log.warn("Обновление пользователя отклонено: email={} уже принадлежит пользователю id={}",
                    user.getEmail(), oldUser.getId());
            throw new EmailAlreadyExistsException("Пользователь с таким email уже существует.");
        }
        userDAO.update(user);
        log.info("Пользователь успешно обновлён: id={}", user.getId());
    }

    public void delete(int id) {
        log.info("Удаление пользователя: id={}", id);
        boolean deleted = userDAO.delete(id);
        if (!deleted) {
            log.warn("Удаление пользователя невозможно: пользователь id={} не найден", id);
            throw new NotFoundException("Пользователь c id=" + id + " не найден");
        }
        log.info("Пользователь успешно удалён: id={}", id);
    }

    public User getById(int id) {
        log.info("Поиск пользователя по id={}", id);
        User user = userDAO.findUserById(id).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        log.info("Пользователь найден: id={}, email={}", user.getId(), user.getEmail());
        return user;
    }

    public List<User> getAll() {
        log.info("Получение списка всех пользователей");
        List<User> users = userDAO.findAll();
        log.info("Получен список пользователей: количество={}", users.size());
        return users;
    }
}
