package ru.yandex.transfer.contract;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.apache.coyote.BadRequestException;
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
import ru.yandex.transfer.model.ExternalTransferRequest;
import ru.yandex.transfer.model.SelfTransferRequest;
import ru.yandex.transfer.service.BlockerService;
import ru.yandex.transfer.service.NotificationService;
import ru.yandex.transfer.service.TransferService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMessageVerifier
@AutoConfigureMockMvc
@Import(StubSecurityConfig.class)
public abstract class BaseContractTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    private TransferService transferService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BlockerService blockerService;
    @MockBean
    private NotificationService notificationService;

    @BeforeEach
    void setup() throws BadRequestException {

        RestAssuredMockMvc.mockMvc(mockMvc);

        when(transferService.externalTransfer(any(ExternalTransferRequest.class))).thenReturn(true);
        when(transferService.selfTransfer(any(SelfTransferRequest.class))).thenReturn(true);

    }
}
