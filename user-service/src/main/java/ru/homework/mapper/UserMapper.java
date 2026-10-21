package ru.homework.mapper;

import ru.homework.dto.UserRequest;
import ru.homework.dto.UserResponse;
import ru.homework.model.User;

public class UserMapper {
    public static User toEntity(UserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setAge(request.getAge());
        return user;
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getAge(), user.getCreatedAt());
    }
}
