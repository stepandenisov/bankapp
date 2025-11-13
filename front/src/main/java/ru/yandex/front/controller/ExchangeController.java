package ru.yandex.front.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.front.model.ExchangeRate;
import ru.yandex.front.model.ExchangeRateResponse;
import ru.yandex.front.service.CurrencyService;

import java.util.List;

@RestController
@RequestMapping("/exchange")
@RequiredArgsConstructor
public class ExchangeController {

    private final CurrencyService currencyService;

    @GetMapping
    public ExchangeRateResponse getExchangeRate(){
        return currencyService.getExchangeRate();
    }

}
