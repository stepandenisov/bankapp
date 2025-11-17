package ru.yandex.account.integration.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.account.dao.UserRepository;
import ru.yandex.account.integration.BaseIntegrationTest;
import ru.yandex.account.model.User;
import ru.yandex.account.model.dto.LoginRequest;
import ru.yandex.account.model.dto.RegisterRequest;

import java.time.LocalDate;


import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(username = "testuser")
@EmbeddedKafka(
        topics = {"notifications"},
        partitions = 1
)
public class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @BeforeAll
    static void init(@Autowired EmbeddedKafkaBroker broker) {
        System.setProperty("spring.kafka.bootstrap-servers", broker.getBrokersAsString());
    }

    @Test
    void register_shouldCreateNewUser() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "newuser",
                "password123",
                "password123",
                "Иван Иванов",
                LocalDate.of(1990, 1, 1)
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Пользователь успешно зарегистрирован"));

        User saved = userRepository.findByUsername("newuser").orElseThrow();
        assertThat(saved.getFullName()).isEqualTo("Иван Иванов");
    }

    @Test
    void register_shouldFailIfUserExists() throws Exception {
        User existing = new User(null, "existing", passwordEncoder.encode("pass"), "Existing User", null, "USER", null);
        userRepository.save(existing);

        RegisterRequest req = new RegisterRequest(
                "existing",
                "password123",
                "password123",
                "Новый пользователь",
                LocalDate.of(1995, 5, 5)
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Пользователь с таким логином уже существует"));
    }

    @Test
    void register_shouldFailIfPasswordsDoNotMatch() throws Exception {
        RegisterRequest req = new RegisterRequest(
                "user2",
                "password1",
                "password2",
                "Test User",
                LocalDate.of(1995, 5, 5)
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Пароли не совпадают"));
    }

    @Test
    void login_shouldReturnToken_whenCredentialsValid() throws Exception {
        User user = new User(null, "validuser", passwordEncoder.encode("secret"), "Valid User", null, "USER", null);
        userRepository.save(user);

        LoginRequest req = new LoginRequest("validuser", "secret");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void login_shouldFailWithInvalidPassword() throws Exception {
        User user = new User(null, "wrongpass", passwordEncoder.encode("right"), "Wrong Pass User", null, "USER", null);
        userRepository.save(user);

        LoginRequest req = new LoginRequest("wrongpass", "wrong");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Неверное имя пользователя или пароль"));
    }
}