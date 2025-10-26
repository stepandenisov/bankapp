package ru.yandex.account.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.account.dao.UserRepository;
import ru.yandex.account.integration.BaseIntegrationTest;
import ru.yandex.account.model.User;
import ru.yandex.account.model.dto.EditPasswordRequest;
import ru.yandex.account.model.dto.EditUserInfoRequest;
import ru.yandex.account.service.JwtService;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = new User(
                null,
                "testuser",
                passwordEncoder.encode("password"),
                "Test User",
                LocalDate.of(1995, 5, 5),
                "USER",
                null
        );
        testUser = userRepository.save(testUser);

        token = jwtService.createToken(org.springframework.security.core.userdetails.User
                .withUsername(testUser.getUsername())
                .password(testUser.getPassword())
                .roles("USER")
                .build());
    }

    @Test
    void getUserInfo_shouldReturnUserDto() throws Exception {
        mockMvc.perform(get("/user")
                        .header("Authorization", "Jwt " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.fullName").value("Test User"));
    }

    @Test
    void getUsers_shouldReturnList() throws Exception {
        mockMvc.perform(get("/user/all")
                        .header("Authorization", "Jwt " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    void editPassword_shouldUpdatePassword() throws Exception {
        EditPasswordRequest request = new EditPasswordRequest("newpass", "newpass");

        mockMvc.perform(post("/user/password")
                        .header("Authorization", "Jwt " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        User updated = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("newpass", updated.getPassword())).isTrue();
    }

    @Test
    void editUserInfo_shouldUpdateFullName() throws Exception {
        EditUserInfoRequest request = new EditUserInfoRequest("Updated Name", LocalDate.of(2000, 1, 1));

        mockMvc.perform(post("/user/info")
                        .header("Authorization", "Jwt " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        User updated = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updated.getFullName()).isEqualTo("Updated Name");
    }

    @Test
    void deleteUser_shouldRemoveFromDb() throws Exception {
        mockMvc.perform(delete("/user")
                        .header("Authorization", "Jwt " + token))
                .andExpect(status().isOk());

        assertThat(userRepository.findById(testUser.getId())).isEmpty();
    }

}
