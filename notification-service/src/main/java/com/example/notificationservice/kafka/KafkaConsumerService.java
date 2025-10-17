package com.example.notificationservice.kafka;

import com.example.notificationservice.dto.UserEventDto;
import com.example.notificationservice.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    private final EmailService emailService;

    public KafkaConsumerService(EmailService emailService) {
        this.emailService = emailService;
    }

    // Метод будет вызван, когда в топик придет новое сообщение.
    @KafkaListener(topics = "user-events", groupId = "notification-group")
    public void consumeUserEvent(UserEventDto event) {
        log.info("Получено событие из Kafka: {}", event);

        // формируем тему и текст письма
        String subject = "";
        String text = "";

        switch (event.eventType()) {
            case USER_CREATED:
                subject = "Добро пожаловать!";
                text = String.format("Здравствуйте, %s! Ваш аккаунт на сайте 'Мой Супер Сайт' был успешно создан.", event.name());
                break;
            case USER_DELETED:
                subject = "Ваш аккаунт был удален";
                text = String.format("Здравствуйте, %s! Ваш аккаунт был удалён с сайта 'Мой Супер Сайт'.", event.name());
                break;
            default:
                log.warn("Получен неизвестный тип события: {}", event.eventType());
                // Ничего не делаем, если тип события неизвестен
                return;
        }

        // Отправляем email, используя наш EmailService
        emailService.sendEmail(event.email(), subject, text);
    }
}