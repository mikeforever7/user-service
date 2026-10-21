package ru.homework.notification.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.homework.notification.dto.SendEmailRequest;
import ru.homework.notification.service.EmailService;

@RestController
@RequestMapping("/api/emails")
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendEmail(@Valid @RequestBody SendEmailRequest request) {
        emailService.sendCustomEmail(
                request.email(),
                request.subject(),
                request.body()
        );
    }
}
