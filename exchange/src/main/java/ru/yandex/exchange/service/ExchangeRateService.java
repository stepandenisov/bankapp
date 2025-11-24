package ru.yandex.exchange.service;

import io.micrometer.tracing.Tracer;
import jakarta.ws.rs.BadRequestException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.yandex.exchange.model.Currency;
import ru.yandex.exchange.model.Exchange;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    @Setter
    @Getter
    private List<Exchange> rates;

    private static final Logger log = LoggerFactory.getLogger(ExchangeRateService.class);

    private final Tracer tracer;


    private Double getRateByCurrency(Currency currency) {
        return rates.stream()
                .filter(rate -> rate.getCurrency().equals(currency))
                .findFirst()
                .map(Exchange::getValue)
                .orElseThrow(() -> {
                    ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
                    ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
                    log.warn("Failed rate by currency operation");
                    ThreadContext.clearAll();
                    return new BadRequestException("Нет такой валюты.");
                });
    }

    public Double convert(Currency from, Currency to, Double amount) {
        Double fromRate = getRateByCurrency(from);
        Double toRate = getRateByCurrency(to);
        return amount * fromRate / toRate;
    }

}
