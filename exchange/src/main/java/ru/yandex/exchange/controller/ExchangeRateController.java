package ru.yandex.exchange.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.exchange.model.Currency;
import ru.yandex.exchange.model.ExchangeRateRequest;
import ru.yandex.exchange.model.ExchangeRateResponse;
import ru.yandex.exchange.model.Exchange;
import ru.yandex.exchange.service.ExchangeRateService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rate")
@CrossOrigin
public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    @PostMapping(path = {"/", ""})
    public void setExchangeRate(@RequestBody ExchangeRateRequest request) {
        exchangeRateService.setRates(request.getExchangeRate());
    }

    @GetMapping(path = "/convert")
    public ResponseEntity<Exchange> getExchangeInfo(@RequestParam("from") Currency from,
                                                    @RequestParam("to") Currency to,
                                                    @RequestParam("amount") Double amount) {
        Double convertedAmount = exchangeRateService.convert(from, to, amount);
        return ResponseEntity.ok(new Exchange(to, convertedAmount));
    }

    @GetMapping(path = {"/", ""})
    public ResponseEntity<ExchangeRateResponse> getExchangeRate() {
        return ResponseEntity.ok(new ExchangeRateResponse(exchangeRateService.getRates()));
    }

    @GetMapping(path = {"/currencies"})
    public ResponseEntity<List<Currency>> getCurrencies() {
        return ResponseEntity.ok(List.of(Currency.values()));
    }

}
