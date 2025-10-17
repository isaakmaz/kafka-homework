package com.example.notificationservice.controller;

import com.example.notificationservice.dto.EmailRequestDto;
import com.example.notificationservice.service.EmailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final EmailService emailService;

    public NotificationController(EmailService emailService) {
        this.emailService = emailService;
    }

    @PostMapping("/send-email")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequestDto request) {
        emailService.sendEmail(
                request.to(),
                request.subject(),
                request.body()
        );
        return ResponseEntity.ok("Запрос на отправку письма принят.");
    }
}