package ru.yandex.exchange.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.yandex.exchange.model.ExchangeRateRequest;
import ru.yandex.exchange.service.ExchangeRateService;

@Service
@RequiredArgsConstructor
public class ExchangeRateListener {

    private final ExchangeRateService exchangeRateService;

    @KafkaListener(topics = "exchange", groupId = "exchange-service")
    public void handle(ExchangeRateRequest request) {
        try {
            exchangeRateService.setRates(request.getExchangeRate());
        } catch (Exception ignored){ }
    }
}