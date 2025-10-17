package com.example.notificationservice.dto;

// DTO для получения запроса на отправку email
public record EmailRequestDto(
        String to,
        String subject,
        String body
) {
}