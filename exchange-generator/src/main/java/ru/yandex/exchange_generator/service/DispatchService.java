package ru.yandex.exchange_generator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.exchange_generator.model.Currency;
import ru.yandex.exchange_generator.model.ExchangeRateRequest;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DispatchService {

    private final GenerationService generationService;

    private final RestTemplate restTemplate;

    @Scheduled(fixedRate = 1000)
    public void sendExchangeRate() {
        Map<Currency, Double> rate = Arrays.stream(Currency.values()).
                collect(Collectors.toMap(
                        currency -> currency,
                        generationService::generateForCurrency
                ));

        ExchangeRateRequest request = new ExchangeRateRequest(rate);
        HttpEntity<ExchangeRateRequest> requestEntity = new HttpEntity<>(request);
        restTemplate.exchange("http://exchange/rate",
                        HttpMethod.POST,
                        requestEntity,
                        String.class)
                .getStatusCode().is2xxSuccessful();
    }

}
