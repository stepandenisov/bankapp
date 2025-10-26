package ru.yandex.exchange.contract;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.exchange.model.Currency;
import ru.yandex.exchange.model.Exchange;
import ru.yandex.exchange.service.ExchangeRateService;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMessageVerifier
@AutoConfigureMockMvc
@Import(StubSecurityConfig.class)
public abstract class BaseContractTest {

    @MockBean
    protected ExchangeRateService exchangeRateService;

    @Autowired
    protected MockMvc mockMvc;

    @BeforeEach
    void setup() {

        RestAssuredMockMvc.mockMvc(mockMvc);

        given(exchangeRateService.convert(eq(Currency.USD), eq(Currency.RUB), eq(100.0)))
                .willReturn(9000.0);

        given(exchangeRateService.getRates()).willReturn(List.of(
                new Exchange(Currency.USD, 1.0),
                new Exchange(Currency.RUB, 90.0)
        ));
    }
}
