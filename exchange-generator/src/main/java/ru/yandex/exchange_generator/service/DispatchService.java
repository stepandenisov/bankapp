package ru.yandex.exchange_generator.service;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(DispatchService.class);

    private final Tracer tracer;

    public void sendExchangeRate() {
        List<Exchange> rate = Arrays.stream(Currency.values())
                .map(currency -> new Exchange(currency, generationService.generateForCurrency(currency)))
                .toList();

        ExchangeRateRequest request = new ExchangeRateRequest(rate);
        kafkaTemplate.send(TOPIC, request);

        ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
        ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
        log.debug("Rate generated.");
        ThreadContext.clearAll();
    }


}
