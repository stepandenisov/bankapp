package ru.yandex.front.unit;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;
import ru.yandex.front.model.Currency;
import ru.yandex.front.model.ExchangeRate;
import ru.yandex.front.model.ExchangeRateResponse;
import ru.yandex.front.service.CurrencyService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class CurrencyServiceUnitTest {

    private RestTemplate restTemplate;
    private CircuitBreakerRegistry cbRegistry;
    private RetryRegistry retryRegistry;
    private CurrencyService currencyService;

    private CircuitBreaker cb;
    private Retry retry;

    @BeforeEach
    void setup() {
        restTemplate = mock(RestTemplate.class);

        cbRegistry = mock(CircuitBreakerRegistry.class);
        retryRegistry = mock(RetryRegistry.class);

        cb = CircuitBreaker.ofDefaults("currencyApi");
        retry = Retry.ofDefaults("currencyApi");

        when(cbRegistry.circuitBreaker("currencyApi")).thenReturn(cb);
        when(retryRegistry.retry("currencyApi")).thenReturn(retry);

        currencyService = new CurrencyService(restTemplate, cbRegistry, retryRegistry);
        currencyService.setExchangeUri("http://test");
    }

    @Test
    void getExchangeRate_success() {
        ExchangeRate rate = new ExchangeRate(Currency.CNY, 90.0);
        ExchangeRateResponse expected = new ExchangeRateResponse(List.of(rate));

        when(restTemplate.getForObject("http://test/rate", ExchangeRateResponse.class))
                .thenReturn(expected);

        ExchangeRateResponse result = currencyService.getExchangeRate();

        assertEquals(1, result.getRate().size());
        assertEquals(Currency.CNY, result.getRate().get(0).getCurrency());
    }

    @Test
    void getExchangeRate_nullResponse_triggersFallback() {
        when(restTemplate.getForObject("http://test/rate", ExchangeRateResponse.class))
                .thenReturn(null);

        ExchangeRateResponse result = currencyService.getExchangeRate();

        assertTrue(result.getRate().isEmpty());
    }

    @Test
    void getExchangeRate_throwsException_triggersFallback() {
        when(restTemplate.getForObject("http://test/rate", ExchangeRateResponse.class))
                .thenThrow(new RuntimeException("Service unavailable"));

        ExchangeRateResponse result = currencyService.getExchangeRate();

        assertTrue(result.getRate().isEmpty());
    }

    @Test
    void getCurrencies_success() {
        Currency[] currencies = {
                Currency.CNY,
                Currency.USD
        };

        when(restTemplate.getForObject("http://test/rate/currencies", Currency[].class))
                .thenReturn(currencies);

        List<Currency> result = currencyService.getCurrencies();

        assertEquals(2, result.size());
        assertEquals("CNY", result.get(0).name());
    }

    @Test
    void getCurrencies_nullResponse_fallback() {
        when(restTemplate.getForObject("http://test/currencies", Currency[].class))
                .thenReturn(null);

        List<Currency> result = currencyService.getCurrencies();

        assertTrue(result.isEmpty());
    }

    @Test
    void getCurrencies_exception_fallback() {
        when(restTemplate.getForObject("http://test/currencies", Currency[].class))
                .thenThrow(new RuntimeException("Error"));

        List<Currency> result = currencyService.getCurrencies();

        assertTrue(result.isEmpty());
    }
}
