package ru.homework.notification.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EmailServiceTest {

    private JavaMailSender mailSender;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        emailService = new EmailService(mailSender);
    }

    @Test
    void sendUserCreatedEmail_shouldSendCorrectMessage() {
        emailService.sendUserCreatedEmail("user@example.com");
        verify(mailSender).send(argThat((SimpleMailMessage message) ->
                message != null
                        && "user@example.com".equals(message.getTo()[0])
                        && "Аккаунт успешно создан".equals(message.getSubject())
                        && "Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан."
                        .equals(message.getText())
        ));
    }

    @Test
    void sendUserDeletedEmail_shouldSendCorrectMessage() {
        emailService.sendUserDeletedEmail("user@example.com");
        verify(mailSender).send(argThat((SimpleMailMessage message) ->
                message != null
                        && "user@example.com".equals(message.getTo()[0])
                        && "Аккаунт удалён".equals(message.getSubject())
                        && "Здравствуйте! Ваш аккаунт был удалён.".equals(message.getText())
        ));
    }

    @Test
    void sendCustomEmail_shouldSendProvidedMessage() {
        emailService.sendCustomEmail(
                "user@example.com",
                "Тестовая тема",
                "Текст письма"
        );
        verify(mailSender).send(argThat((SimpleMailMessage message) ->
                message != null
                        && "user@example.com".equals(message.getTo()[0])
                        && "Тестовая тема".equals(message.getSubject())
                        && "Текст письма".equals(message.getText())
        ));
    }
}
