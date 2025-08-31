package ru.yandex.exchange.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.exchange.model.Currency;
import ru.yandex.exchange.model.ExchangeRateRequest;
import ru.yandex.exchange.model.ExchangeRateResponse;
import ru.yandex.exchange.model.ExchangeResponse;
import ru.yandex.exchange.service.ExchangeRateService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rate")
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @PostMapping(path = {"/", ""})
    public void setExchangeRate(@RequestBody ExchangeRateRequest request) {
        exchangeRateService.setRate(request.getExchangeRate());
    }

    @GetMapping(path = "/convert")
    public ResponseEntity<ExchangeResponse> getExchangeInfo(@RequestParam("from") Currency from,
                                                            @RequestParam("to") Currency to,
                                                            @RequestParam("amount") Double amount) {
        Double convertedAmount = exchangeRateService.convert(from, to, amount);
        return ResponseEntity.ok(new ExchangeResponse(to, convertedAmount));
    }

    @GetMapping(path = {"/", ""})
    public ResponseEntity<ExchangeRateResponse> getExchangeRate() {
        return ResponseEntity.ok(new ExchangeRateResponse(exchangeRateService.getRate()));
    }

}
