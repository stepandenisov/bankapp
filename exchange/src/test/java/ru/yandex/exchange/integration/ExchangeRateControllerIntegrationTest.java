package ru.yandex.exchange.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.exchange.TestSecurityConfig;
import ru.yandex.exchange.model.Currency;
import ru.yandex.exchange.model.Exchange;
import ru.yandex.exchange.model.ExchangeRateRequest;
import ru.yandex.exchange.model.ExchangeRateResponse;
import ru.yandex.exchange.service.ExchangeRateService;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class ExchangeRateControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExchangeRateService exchangeRateService;

    private List<Exchange> initialRates;

    @BeforeEach
    void setUp() {
        initialRates = List.of(
                new Exchange(Currency.USD, 1.0),
                new Exchange(Currency.CNY, 0.9),
                new Exchange(Currency.RUB, 80.0)
        );
        exchangeRateService.setRates(initialRates);
    }

    @Test
    void setExchangeRate_ShouldUpdateRates() throws Exception {
        List<Exchange> newRates = List.of(
                new Exchange(Currency.USD, 1.0),
                new Exchange(Currency.CNY, 0.95)
        );
        ExchangeRateRequest request = new ExchangeRateRequest();
        request.setExchangeRate(newRates);

        mockMvc.perform(post("/rate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        List<Exchange> ratesInService = exchangeRateService.getRates();
        assertThat(ratesInService, hasSize(2));
        assertThat(ratesInService, hasItem(hasProperty("currency", equalTo(Currency.CNY))));
        assertThat(ratesInService, hasItem(hasProperty("value", equalTo(0.95))));
    }

    @Test
    void getExchangeInfo_ShouldReturnConvertedAmount() throws Exception {
        double amount = 100.0;

        mockMvc.perform(get("/rate/convert")
                        .param("from", "USD")
                        .param("to", "CNY")
                        .param("amount", String.valueOf(amount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency", is("CNY")))
                .andExpect(jsonPath("$.value", closeTo(amount / 0.9, 0.0001)));
    }

    @Test
    void getExchangeRate_ShouldReturnCurrentRates() throws Exception {
        mockMvc.perform(get("/rate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rate", hasSize(initialRates.size())))
                .andExpect(jsonPath("$.rate[?(@.currency=='USD')].value", contains(1.0)))
                .andExpect(jsonPath("$.rate[?(@.currency=='CNY')].value", contains(0.9)));
    }

    @Test
    void getCurrencies_ShouldReturnAllCurrencies() throws Exception {
        mockMvc.perform(get("/rate/currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(Currency.values().length)))
                .andExpect(jsonPath("$", hasItem("USD")))
                .andExpect(jsonPath("$", hasItem("CNY")))
                .andExpect(jsonPath("$", hasItem("RUB")));
    }
}
