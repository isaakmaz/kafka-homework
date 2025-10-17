package com.example.userservicespring.controller;

import com.example.userservicespring.dto.CreateUserRequestDto;
import com.example.userservicespring.dto.UserDto;
import com.example.userservicespring.exception.UserNotFoundException;
import com.example.userservicespring.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService userService;

    @Test
    void getUserById_whenUserExists_shouldReturnUserDtoAndStatus200() throws Exception {
        UserDto fakeUser = new UserDto(1L, "Mark Palc", "marc.palc@example.com", 30);
        when(userService.findById(1L)).thenReturn(Optional.of(fakeUser));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Mark Palc"))
                .andExpect(jsonPath("$.email").value("marc.palc@example.com"));
    }

    @Test
    void getUserById_whenUserDoesNotExist_shouldReturnStatus404() throws Exception {
        when(userService.findById(999L)).thenThrow(new UserNotFoundException("User not found with id: 999"));

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_whenUserExists_shouldReturnUpdatedUser() throws Exception {
        long userId = 1L;
        CreateUserRequestDto requestDto = new CreateUserRequestDto("Екатерина Блу", "kate.blue@example.ru", 31);
        UserDto updatedUserDto = new UserDto(userId, "Екатерина Блу", "kate.blue@example.ru", 31);
        when(userService.update(eq(userId), any(CreateUserRequestDto.class))).thenReturn(updatedUserDto);

        mockMvc.perform(put("/api/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Екатерина Блу"))
                .andExpect(jsonPath("$.email").value("kate.blue@example.ru"));
    }

    @Test
    void deleteUser_whenUserExists_shouldReturnNoContent() throws Exception {
        long userId = 1L;

        mockMvc.perform(delete("/api/users/" + userId))
                .andExpect(status().isNoContent());
    }
}