package ru.homework.gateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class FallbackController {

    @GetMapping("/fallback/users")
    public Mono<String> usersFallback() {
        return Mono.just("User Service временно недоступен. Попробуйте позже.");
    }

    @GetMapping("/fallback/notifications")
    public Mono<String> notificationsFallback() {
        return Mono.just("Notification Service временно недоступен. Попробуйте позже.");
    }
}