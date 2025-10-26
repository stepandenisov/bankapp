package ru.yandex.notifications.contract;

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
import ru.yandex.notifications.model.NotificationRequest;
import ru.yandex.notifications.service.NotificationService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMessageVerifier
@AutoConfigureMockMvc
@Import(StubSecurityConfig.class)
public abstract class BaseContractTest {

    @LocalServerPort
    protected int port;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    protected MockMvc mockMvc;

    @BeforeEach
    void setup() {

        RestAssuredMockMvc.mockMvc(mockMvc);
        doNothing().when(notificationService).send(any(NotificationRequest.class));

    }
}
