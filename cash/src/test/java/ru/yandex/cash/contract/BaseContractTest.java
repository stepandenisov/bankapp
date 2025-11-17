package ru.yandex.cash.contract;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.cash.configuration.TestSecurityConfig;
import ru.yandex.cash.service.CashService;
import ru.yandex.cash.model.CashRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMessageVerifier
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@EmbeddedKafka(
        topics = {"notifications"},
        partitions = 1
)

public abstract class BaseContractTest {

    @MockBean
    protected CashService cashService;

    @Autowired
    protected MockMvc mockMvc;

    @BeforeAll
    static void init(@Autowired EmbeddedKafkaBroker broker) {
        System.setProperty("spring.kafka.bootstrap-servers", broker.getBrokersAsString());
    }

    @BeforeEach
    void setup() {

        RestAssuredMockMvc.mockMvc(mockMvc);

        given(cashService.withdraw(eq(123L), any(CashRequest.class))).willAnswer(invocation -> {
            CashRequest req = invocation.getArgument(1);
            return req.getAmount() <= 500;
        });

        given(cashService.topUp(eq(123L), any(CashRequest.class))).willAnswer(invocation -> {
            CashRequest req = invocation.getArgument(1);
            return req.getAmount() > 0;
        });
    }
}
