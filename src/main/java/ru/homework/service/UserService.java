package ru.homework.service;

import ru.homework.dto.UserRequest;
import ru.homework.dto.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse create(UserRequest request);
    UserResponse update(Long id, UserRequest request);
    void delete(Long id);
    UserResponse getById(Long id);
    List<UserResponse> getAll();
}
