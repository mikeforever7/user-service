package ru.homework.notification.service;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "spring.mail.host=127.0.0.1",
        "spring.mail.port=3025",
        "spring.mail.username=",
        "spring.mail.password=",
        "spring.mail.properties.mail.smtp.auth=false",
        "spring.mail.properties.mail.smtp.starttls.enable=false"
})
class EmailServiceIntegrationTest {

    @RegisterExtension
    static final GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP);

    @Autowired
    private EmailService emailService;

    @BeforeEach
    void cleanMailbox() throws FolderException {
        greenMail.purgeEmailFromAllMailboxes();
    }

    @Test
    void sendUserCreatedEmail_shouldDeliverMessageToSmtpServer() throws Exception {
        emailService.sendUserCreatedEmail("user@example.com");
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        assertEquals(
                "user@example.com",
                messages[0].getAllRecipients()[0].toString()
        );
        assertEquals("Аккаунт успешно создан", messages[0].getSubject());
        assertEquals(
                "Здравствуйте! Ваш аккаунт на сайте ваш сайт был успешно создан.",
                messages[0].getContent().toString().trim()
        );
    }

    @Test
    void sendUserDeletedEmail_shouldDeliverMessageToSmtpServer() throws Exception {
        emailService.sendUserDeletedEmail("user@example.com");
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        assertEquals("Аккаунт удалён", messages[0].getSubject());
        assertEquals(
                "Здравствуйте! Ваш аккаунт был удалён.",
                messages[0].getContent().toString().trim()
        );
    }

    @Test
    void sendCustomEmail_shouldDeliverCustomMessage() throws Exception {
        emailService.sendCustomEmail(
                "user@example.com",
                "Тестовая тема",
                "Текст письма"
        );
        MimeMessage[] messages = greenMail.getReceivedMessages();
        assertEquals(1, messages.length);
        assertEquals("Тестовая тема", messages[0].getSubject());
        assertEquals(
                "Текст письма",
                messages[0].getContent().toString().trim()
        );
    }
}