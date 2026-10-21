package ru.homework.notification.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.homework.notification.event.UserEvent;
import ru.homework.notification.event.UserOperation;
import ru.homework.notification.service.EmailService;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

    private final EmailService emailService;

    @KafkaListener(topics = "user-events", groupId = "notification-service-group")
    public void handle(UserEvent event) {
        log.info(
                "Получено событие пользователя: operation={}, email={}",
                event.operation(),
                event.email()
        );

        if (event.operation() == UserOperation.CREATED) {
            emailService.sendUserCreatedEmail(event.email());
        } else if (event.operation() == UserOperation.DELETED) {
            emailService.sendUserDeletedEmail(event.email());
        } else {
            log.warn("Неизвестная операция пользователя: {}", event.operation());
        }
    }
}
