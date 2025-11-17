package ru.yandex.exchange_generator.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.exchange_generator.model.Currency;
import ru.yandex.exchange_generator.model.Exchange;
import ru.yandex.exchange_generator.model.ExchangeRateRequest;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DispatchService {

    private final GenerationService generationService;

    private final KafkaTemplate<String, ExchangeRateRequest> kafkaTemplate;

    private static final String TOPIC = "exchange";

    public void sendExchangeRate() {
        List<Exchange> rate = Arrays.stream(Currency.values())
                .map(currency -> new Exchange(currency, generationService.generateForCurrency(currency)))
                .toList();

        ExchangeRateRequest request = new ExchangeRateRequest(rate);
        kafkaTemplate.send(TOPIC, request);
    }


}
