package ru.yandex.exchange_generator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.yandex.exchange_generator.model.Currency;
import ru.yandex.exchange_generator.model.Exchange;
import ru.yandex.exchange_generator.model.ExchangeRateRequest;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DispatchService {

    private final GenerationService generationService;

    private final RestTemplate restTemplate;

    @Scheduled(fixedRate = 1000)
    public void sendExchangeRate() {
        List<Exchange> rate = Arrays.stream(Currency.values()).
                map(currency -> new Exchange(currency, generationService.generateForCurrency(currency)))
                .toList();

        ExchangeRateRequest request = new ExchangeRateRequest(rate);
        HttpEntity<ExchangeRateRequest> requestEntity = new HttpEntity<>(request);
        restTemplate.exchange("http://exchange/rate",
                        HttpMethod.POST,
                        requestEntity,
                        String.class)
                .getStatusCode().is2xxSuccessful();
    }

}
