package com.example.userservicespring.service;

import com.example.userservicespring.dto.CreateUserRequestDto;
import com.example.userservicespring.dto.EventType;
import com.example.userservicespring.dto.UserDto;
import com.example.userservicespring.dto.UserEventDto;
import com.example.userservicespring.entity.User;
import com.example.userservicespring.exception.UserNotFoundException;
import com.example.userservicespring.kafka.KafkaProducerService;
import com.example.userservicespring.mapper.UserMapper;
import com.example.userservicespring.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final KafkaProducerService kafkaProducerService; // Наша новая зависимость

    // Внедряем обе зависимости через конструктор
    public UserServiceImpl(UserRepository userRepository, KafkaProducerService kafkaProducerService) {
        this.userRepository = userRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Override
    @Transactional
    public UserDto save(CreateUserRequestDto requestDto) {
        User user = UserMapper.toEntity(requestDto);
        User savedUser = userRepository.save(user);

        // После успешного сохранения отправляем событие в Kafka
        UserEventDto event = new UserEventDto(EventType.USER_CREATED, savedUser.getEmail(), savedUser.getName());
        kafkaProducerService.sendUserEvent(event);

        return UserMapper.toDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDto> findById(Long id) {
        return userRepository.findById(id)
                .map(UserMapper::toDto);
    }

    @Override
    @Transactional
    public UserDto update(Long id, CreateUserRequestDto requestDto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        existingUser.setName(requestDto.name());
        existingUser.setEmail(requestDto.email());
        existingUser.setAge(requestDto.age());

        User updatedUser = userRepository.save(existingUser);
        return UserMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // Сначала находим пользователя, чтобы получить его данные для Kafka
        User userToDelete = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Cannot delete. User not found with id: " + id));

        // Теперь удаляем
        userRepository.deleteById(id);

        // После успешного удаления отправляем событие
        UserEventDto event = new UserEventDto(EventType.USER_DELETED, userToDelete.getEmail(), userToDelete.getName());
        kafkaProducerService.sendUserEvent(event);
    }
}