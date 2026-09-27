import exception.EmailAlreadyExistsException;
import exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import model.User;
import org.hibernate.HibernateException;

import java.util.List;
import java.util.Scanner;

@Slf4j
public class ConsoleMenu {
    private final Scanner scanner;
    private final UserService userService;

    public ConsoleMenu(Scanner scanner1, UserService userService) {
        this.scanner = scanner1;
        this.userService = userService;
    }

    public void run() {
        while (true) {
            printMenu();
            String choice = scanner.nextLine();
            try {
                switch (choice) {
                    case "1" -> {
                        log.info("Выбрана операция: создание пользователя");
                        createUser();
                    }
                    case "2" -> {
                        log.info("Выбрана операция: поиск пользователя по ID");
                        findUserById();
                    }
                    case "3" -> {
                        log.info("Выбрана операция: получение всех пользователей");
                        findAllUsers();
                    }
                    case "4" -> {
                        log.info("Выбрана операция: обновление пользователя");
                        updateUser();
                    }
                    case "5" -> {
                        log.info("Выбрана операция: удаление пользователя");
                        deleteUser();
                    }
                    case "0" -> {
                        log.info("Пользователь завершил работу приложения");
                        System.out.println("До свидания!");
                        return;
                    }
                    default -> {
                        log.warn("Неизвестная команда: {}", choice);
                        System.out.println("Неизвестная команда. Попробуйте ещё раз.");
                    }
                }
            } catch (HibernateException e) {
                log.error("Ошибка Hibernate", e);
                System.out.println("Ошибка при работе с базой данных.");
            } catch (Exception e) {
                log.error("Необработанная ошибка", e);
                System.out.println("Произошла ошибка при работе с системой.");
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("==============================");
        System.out.println(" СЕРВИС ПОЛЬЗОВАТЕЛЕЙ");
        System.out.println("==============================");
        System.out.println("1. Создать пользователя");
        System.out.println("2. Найти пользователя по ID");
        System.out.println("3. Показать всех пользователей");
        System.out.println("4. Обновить пользователя");
        System.out.println("5. Удалить пользователя");
        System.out.println("0. Выйти");
        System.out.println("==============================");
        System.out.print("Выберите действие: ");
    }

    private void printUpdateMenu() {
        System.out.println("1. Обновить имя");
        System.out.println("2. Обновить email");
        System.out.println("3. Обновить возраст");
        System.out.println("4. Сохранить измененного пользователя");
        System.out.println("0. Выйти без сохранения");
    }

    private void createUser() {
        System.out.println("\n=== Создание пользователя ===");
        String name = readName();
        String email = readEmail();
        int age = readAge();

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setAge(age);
        try {
            userService.create(user);
            System.out.println("Пользователь успешно создан.");
            System.out.println(user);
        } catch (EmailAlreadyExistsException e) {
            System.out.println("Ошибка: " + e.getMessage());
        }
    }


    private void findUserById() {
        System.out.println("\n=== Ищем пользователя ===");
        int id = readUserId();
        if (id == 0) {
            return;
        }
        try {
            User user = userService.getById(id);
            System.out.println("Пользователь найден:");
            System.out.println(user);
        } catch (NotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private void findAllUsers() {
        List<User> users = userService.getAll();
        if (users.isEmpty()) {
            System.out.println("Список пользователей пуст");
            return;
        }
        users.forEach(System.out::println);
    }

    private void updateUser() {
        System.out.println("\n=== Обновляем пользователя ===");
        User user;
        int id = readUserId();
        if (id == 0) {
            return;
        }
        try {
            user = userService.getById(id);
        } catch (NotFoundException e) {
            System.out.println(e.getMessage());
            return;
        }
        System.out.println("Пользователь найден!");
        System.out.println("Выберите поля для обновления:");
        boolean updating = true;
        while (updating) {
            printUpdateMenu();
            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> user.setName(readName());
                case "2" -> user.setEmail(readEmail());
                case "3" -> user.setAge(readAge());
                case "4" -> {
                    try {
                        userService.update(user);
                        System.out.println("Пользователь успешно обновлён.");
                        return;
                    } catch (EmailAlreadyExistsException e) {
                        System.out.println("Ошибка: " + e.getMessage());
                    }
                }
                case "0" -> updating = false;
                default -> System.out.println("Неизвестная команда. Попробуйте ещё раз.");
            }
        }
    }

    private void deleteUser() {
        System.out.println("\n=== Удаляем пользователя ===");
        int id = readUserId();
        if (id == 0) {
            return;
        }
        try {
            userService.delete(id);
            System.out.println("Пользователь с id=" + id + " успешно удалён");
        } catch (NotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    private String readName() {
        while (true) {
            System.out.print("Введите имя: ");
            String name = scanner.nextLine();
            if (!name.isBlank()) {
                return name;
            }
            System.out.println("Ошибка: имя не может быть пустым.");
        }
    }

    private String readEmail() {
        while (true) {
            System.out.print("Введите email: ");
            String email = scanner.nextLine();
            if (email.contains("@")) {
                return email;
            }
            System.out.println("Ошибка: некорректный email.");
        }
    }

    private int readAge() {
        while (true) {
            System.out.print("Введите возраст: ");
            try {
                int age = Integer.parseInt(scanner.nextLine());
                if (age >= 0 && age <= 150) {
                    return age;
                }
                System.out.println("Ошибка: возраст должен быть от 0 до 150.");
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число.");
            }
        }
    }

    private int readUserId() {
        while (true) {
            System.out.println("0 - выход в стартовое меню");
            System.out.print("Введите id пользователя: ");
            try {
                int id = Integer.parseInt(scanner.nextLine());
                if (id == 0) {
                    return 0;
                }
                if (id < 0) {
                    log.warn("Введён некорректный ID: {}", id);
                    System.out.println("Ошибка: ID должен быть положительным числом.");
                    continue;
                }
                return id;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число.");
            }
        }
    }
}
