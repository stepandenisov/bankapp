package ru.yandex.front.contract;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.front.TestSecurityConfig;
import ru.yandex.front.model.EditPasswordRequest;
import ru.yandex.front.model.EditUserInfoRequest;
import ru.yandex.front.model.RegisterRequest;
import ru.yandex.front.model.UserDto;
import ru.yandex.front.service.UserService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMessageVerifier
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
public abstract class BaseContractTest {

    @LocalServerPort
    protected int port;


    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected UserService userService;

    @BeforeEach
    void setup() {

        RestAssuredMockMvc.mockMvc(mockMvc);

        given(userService.getUsers()).willReturn(List.of(
                new UserDto(1L, "user1", "User One", LocalDate.of(1990, 1, 1)),
                new UserDto(2L, "user2", "User Two", LocalDate.of(1995, 5, 15))
        ));

        doNothing().when(userService).register(any(RegisterRequest.class));
        doNothing().when(userService).editPassword(any(EditPasswordRequest.class));
        doNothing().when(userService).editInfo(any(EditUserInfoRequest.class));

    }
}
