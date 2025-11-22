package ru.yandex.exchange.listener;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.yandex.exchange.model.ExchangeRateRequest;
import ru.yandex.exchange.service.ExchangeRateService;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ExchangeRateListener {

    private final ExchangeRateService exchangeRateService;

    private final AtomicLong lastUpdateTime = new AtomicLong(System.currentTimeMillis());

    public ExchangeRateListener(ExchangeRateService exchangeRateService, MeterRegistry registry) {
        this.exchangeRateService = exchangeRateService;
        Gauge.builder("exchange_last_update_seconds",
                () -> Duration.between(
                        Instant.ofEpochMilli(lastUpdateTime.get()),
                        Instant.now()
                ).getSeconds()
        ).register(registry);
    }

    @KafkaListener(topics = "exchange", groupId = "exchange-service")
    public void handle(ExchangeRateRequest request) {
        try {
            exchangeRateService.setRates(request.getExchangeRate());
            lastUpdateTime.set(System.currentTimeMillis());
        } catch (Exception ignored){ }
    }
}