package com.example.userservicespring.mapper;

import com.example.userservicespring.dto.CreateUserRequestDto;
import com.example.userservicespring.dto.UserDto;
import com.example.userservicespring.entity.User;

import java.time.LocalDateTime;

public class UserMapper {

    public static UserDto toDto(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge()
        );
    }

    public static User toEntity(CreateUserRequestDto requestDto) {
        // Создаем пустой объект
        User user = new User();
        // Устанавливаем поля из DTO
        user.setName(requestDto.name());
        user.setEmail(requestDto.email());
        user.setAge(requestDto.age());

        user.setCreatedAt(LocalDateTime.now());

        // Возвращаем готовый к сохранению в базу объект
        return user;
    }

}
