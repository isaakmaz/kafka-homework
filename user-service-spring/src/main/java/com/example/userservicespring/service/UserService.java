package com.example.userservicespring.service;

import com.example.userservicespring.dto.CreateUserRequestDto;
import com.example.userservicespring.dto.UserDto;
import java.util.Optional;

public interface UserService {
    // Принимаем DTO, возвращаем DTO
    UserDto save(CreateUserRequestDto requestDto);

    // Возвращаем Optional от DTO
    Optional<UserDto> findById(Long id);

    UserDto update(Long id, CreateUserRequestDto requestDto);

    void delete(Long id);
}