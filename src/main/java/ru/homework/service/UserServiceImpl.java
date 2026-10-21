package ru.homework.service;

import org.springframework.context.ApplicationEventPublisher;
import ru.homework.dto.UserRequest;
import ru.homework.dto.UserResponse;
import ru.homework.event.UserEvent;
import ru.homework.event.UserOperation;
import ru.homework.exception.EmailAlreadyExistsException;
import ru.homework.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.homework.mapper.UserMapper;
import ru.homework.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.homework.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @Override
    public UserResponse create(UserRequest request) {
        log.info("Создание пользователя: email={}", request.getEmail());
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Создание пользователя отклонено: email={} уже существует", request.getEmail());
            throw new EmailAlreadyExistsException("Пользователь с таким email уже существует.");
        }
        User user = UserMapper.toEntity(request);
        User savedUser = userRepository.save(user);
        log.info("Пользователь успешно создан: id={}, email={}", savedUser.getId(), savedUser.getEmail());
        eventPublisher.publishEvent(new UserEvent(UserOperation.CREATED, savedUser.getEmail()));
        return UserMapper.toResponse(savedUser);
    }

    @Transactional
    @Override
    public UserResponse update(Long id, UserRequest request) {
        log.info("Обновление пользователя: id={}, email={}", id, request.getEmail());
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Обновление пользователя невозможно: пользователь id={} не найден", id);
                    return new NotFoundException("Пользователь с id=" + id + " не найден");
                });
        userRepository.findByEmail(request.getEmail())
                .filter(oldUser -> !oldUser.getId().equals(id))
                .ifPresent(oldUser -> {
                    log.warn("Обновление пользователя отклонено: email={} уже принадлежит пользователю id={}", request.getEmail(), oldUser.getId());
                    throw new EmailAlreadyExistsException("Пользователь с таким email уже существует.");
                });
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setAge(request.getAge());
        User updatedUser = userRepository.save(user);
        log.info("Пользователь успешно обновлён: id={}", id);
        return UserMapper.toResponse(updatedUser);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        log.info("Удаление пользователя: id={}", id);
        User user = userRepository.findById(id).orElseThrow(() -> {
            log.warn("Удаление пользователя невозможно: пользователь id={} не найден", id);
            return new NotFoundException("Пользователь с id=" + id + " не найден");
        });
        String email = user.getEmail();
        userRepository.delete(user);
        log.info("Пользователь успешно удалён: id={}", id);
        eventPublisher.publishEvent(new UserEvent(UserOperation.DELETED, email));
    }

    @Override
    public UserResponse getById(Long id) {
        log.info("Поиск пользователя по id={}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Получение пользователя невозможно: id={} не найден", id);
                    return new NotFoundException("Пользователь с id=" + id + " не найден");
                });
        log.info("Пользователь найден: id={}, email={}", user.getId(), user.getEmail());
        return UserMapper.toResponse(user);
    }

    @Override
    public List<UserResponse> getAll() {
        log.info("Получение списка всех пользователей");
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(UserMapper::toResponse)
                .toList();
        log.info("Получен список пользователей: количество={}", users.size());
        return users;
    }
}
