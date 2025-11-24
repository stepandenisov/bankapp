package ru.yandex.exchange.listener;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);

    private final Tracer tracer;

    public ExchangeRateListener(ExchangeRateService exchangeRateService, MeterRegistry registry, Tracer tracer) {
        this.exchangeRateService = exchangeRateService;
        Gauge.builder("exchange_last_update_seconds",
                () -> Duration.between(
                        Instant.ofEpochMilli(lastUpdateTime.get()),
                        Instant.now()
                ).getSeconds()
        ).register(registry);
        this.tracer = tracer;
    }

    @KafkaListener(topics = "exchange", groupId = "exchange-service")
    public void handle(ExchangeRateRequest request) {
        try {
            exchangeRateService.setRates(request.getExchangeRate());
            lastUpdateTime.set(System.currentTimeMillis());
            ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
            ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
            log.debug("Rate updated.");
            ThreadContext.clearAll();
        } catch (Exception ignored){ }
    }
}