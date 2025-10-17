package com.example.userservicespring.dto;

// DTO для отправки события о пользователе в Kafka
public record UserEventDto(
        EventType eventType,
        String email,
        String name
) {
}