package ru.yandex.front.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.front.model.Currency;
import ru.yandex.front.service.CurrencyService;

import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CurrencyServiceStubIntegrationTest extends BaseServiceStubIntegrationTest{

    @Autowired
    private CurrencyService currencyService;

    @Test
    void currencyService_getCurrencies_ReturnsList() {
        List<Currency> currencies = currencyService.getCurrencies();
        assertNotNull(currencies);
        assertFalse(currencies.isEmpty());
        assertTrue(currencies.contains(Currency.USD));
        assertTrue(currencies.contains(Currency.CNY));
    }

}
