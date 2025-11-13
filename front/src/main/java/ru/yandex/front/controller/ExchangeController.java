package ru.yandex.front.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.yandex.front.model.ExchangeRate;
import ru.yandex.front.service.CurrencyService;

import java.util.List;

@Controller
@RequestMapping("/exchange")
@RequiredArgsConstructor
public class ExchangeController {

    private final CurrencyService currencyService;

    @GetMapping
    public List<ExchangeRate> getExchangeRate(){
        return currencyService.getExchangeRate();
    }

}
