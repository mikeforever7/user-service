package ru.homework.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendUserCreatedEmail(String email) {
        send(
                email,
                "Аккаунт успешно создан",
                "Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан."
        );
    }

    public void sendUserDeletedEmail(String email) {
        send(
                email,
                "Аккаунт удалён",
                "Здравствуйте! Ваш аккаунт был удалён."
        );
    }

    public void sendCustomEmail(String email, String subject, String body) {
        send(email, subject, body);
    }

    private void send(String email, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}
