package ru.yandex.front.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.front.model.Currency;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CurrencyService {

    private final RestTemplate restTemplate;

    public List<Currency> getCurrencies() {
        return List.of(restTemplate.getForObject("http://exchange/rate/currencies",
                Currency[].class));
    }

}
