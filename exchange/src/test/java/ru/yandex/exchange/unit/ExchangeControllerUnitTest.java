package ru.yandex.exchange.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.exchange.TestSecurityConfig;
import ru.yandex.exchange.controller.ExchangeRateController;
import ru.yandex.exchange.model.Currency;
import ru.yandex.exchange.model.Exchange;
import ru.yandex.exchange.model.ExchangeRateRequest;
import ru.yandex.exchange.model.ExchangeRateResponse;
import ru.yandex.exchange.service.ExchangeRateService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExchangeRateController.class)
@Import(TestSecurityConfig.class)
class ExchangeControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ExchangeRateService exchangeRateService;

    @Test
    void setExchangeRate_shouldCallService() throws Exception {
        List<Exchange> rates = List.of(new Exchange(Currency.RUB, 1.0));
        ExchangeRateRequest req = new ExchangeRateRequest();
        req.setExchangeRate(rates);

        mockMvc.perform(post("/rate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        ArgumentCaptor<List<Exchange>> captor = ArgumentCaptor.forClass(List.class);
        verify(exchangeRateService, times(1)).setRates(captor.capture());
        assertThat(captor.getValue()).contains(new Exchange(Currency.RUB, 1.0));
    }

    @Test
    void getExchangeInfo_shouldReturnConvertedValue() throws Exception {
        when(exchangeRateService.convert(Currency.USD, Currency.RUB, 10.0))
                .thenReturn(925.0);

        mockMvc.perform(get("/rate/convert")
                        .param("from", "USD")
                        .param("to", "RUB")
                        .param("amount", "10.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("RUB"));

        verify(exchangeRateService, times(1))
                .convert(Currency.USD, Currency.RUB, 10.0);
    }

    @Test
    void getExchangeRate_shouldReturnRates() throws Exception {
        List<Exchange> rates = List.of(new Exchange(Currency.RUB, 1.0));
        when(exchangeRateService.getRates()).thenReturn(rates);

        mockMvc.perform(get("/rate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rate[0].value").value(1.0));

        verify(exchangeRateService, times(1)).getRates();
    }

    @Test
    void getCurrencies_shouldReturnAllCurrencies() throws Exception {
        mockMvc.perform(get("/rate/currencies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(Currency.values().length))
                .andExpect(jsonPath("$[0]").value(Currency.values()[0].name()));
    }
}
