package com.example.notificationservice;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.example.notificationservice.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(
        properties = {
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
        })
class EmailServiceIT {

    private static GreenMail greenMail;

    @BeforeAll
    static void startMailServer() {
        greenMail = new GreenMail(ServerSetupTest.SMTP);
        greenMail.withConfiguration(GreenMailConfiguration.aConfig().withUser("test", "test"));
        greenMail.start();
    }

    @AfterAll
    static void stopMailServer() {
        greenMail.stop();
    }

    @DynamicPropertySource
    static void configureMailProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.mail.host", () -> greenMail.getSmtp().getBindTo());
        registry.add("spring.mail.port", () -> greenMail.getSmtp().getPort());
        registry.add("spring.mail.username", () -> "test");
        registry.add("spring.mail.password", () -> "test");
        registry.add("spring.mail.protocol", () -> "smtp");
    }

    @Autowired
    private EmailService emailService;

    @Test
    void shouldSendEmailSuccessfully() throws Exception {
        String to = "test-recipient@example.com";
        String subject = "Тестовое письмо";
        String body = "Это тело тестового письма.";

        emailService.sendEmail(to, subject, body);

        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
            assertThat(receivedMessages).hasSize(1);

            MimeMessage receivedMessage = receivedMessages[0];
            assertThat(receivedMessage.getAllRecipients()[0].toString()).isEqualTo(to);
            assertThat(receivedMessage.getSubject()).isEqualTo(subject);
            assertThat(receivedMessage.getContent().toString()).contains(body);
        });
    }
}
