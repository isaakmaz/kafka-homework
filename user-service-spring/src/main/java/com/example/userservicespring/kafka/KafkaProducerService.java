package com.example.userservicespring.kafka;

import com.example.userservicespring.dto.UserEventDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducerService.class);
    private static final String TOPIC = "user-events";

    private final KafkaTemplate<String, UserEventDto> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, UserEventDto> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUserEvent(UserEventDto eventDto) {
        log.info("Отправка события в Kafka: {}", eventDto);
        // Отправляем сообщение в топик "user-events"
        kafkaTemplate.send(TOPIC, eventDto.email(), eventDto);
    }
}