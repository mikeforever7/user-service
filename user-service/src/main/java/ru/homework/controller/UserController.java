package ru.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import ru.homework.dto.UserRequest;
import ru.homework.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.homework.service.UserService;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API для управления пользователями")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Создать нового пользователя", description = "Создает пользователя и возвращает его данные со ссылками")
    public EntityModel<UserResponse> create(@Valid @RequestBody UserRequest request) {
        UserResponse createdUser = userService.create(request);

        return EntityModel.of(createdUser,
                linkTo(methodOn(UserController.class).getById(createdUser.getId())).withSelfRel(),
                linkTo(methodOn(UserController.class).getAll()).withRel("all-users")
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить пользователя", description = "Обновляет данные пользователя по ID")
    public EntityModel<UserResponse> update(@Parameter(description = "ID пользователя") @PathVariable Long id, @Valid @RequestBody UserRequest request) {
        UserResponse updatedUser = userService.update(id, request);

        return EntityModel.of(updatedUser,
                linkTo(methodOn(UserController.class).getById(id)).withSelfRel()
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя по ID. Возвращает 204 No Content")
    public void delete(@Parameter(description = "ID пользователя") @PathVariable Long id) {
        userService.delete(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить пользователя по ID", description = "Возвращает данные пользователя и ссылки для навигации")
    public EntityModel<UserResponse> getById(@Parameter(description = "ID пользователя") @PathVariable Long id) {
        UserResponse user = userService.getById(id);

        return EntityModel.of(user,
                linkTo(methodOn(UserController.class).getById(id)).withSelfRel(),
                linkTo(methodOn(UserController.class).getAll()).withRel("users"),
                linkTo(UserController.class).slash(id).withRel("delete"));
    }

    @GetMapping
    @Operation(summary = "Получить всех пользователей", description = "Возвращает коллекцию пользователей с ссылками")
    public CollectionModel<EntityModel<UserResponse>> getAll() {
        List<UserResponse> users = userService.getAll();

        List<EntityModel<UserResponse>> userModels = users.stream()
                .map(user -> EntityModel.of(user, linkTo(methodOn(UserController.class).getById(user.getId())).withSelfRel()))
                .toList();

        return CollectionModel.of(userModels, linkTo(methodOn(UserController.class).getAll()).withSelfRel());
    }
}
