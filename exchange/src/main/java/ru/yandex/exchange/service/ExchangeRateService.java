package ru.yandex.exchange.service;

import jakarta.ws.rs.BadRequestException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import ru.yandex.exchange.model.Currency;
import ru.yandex.exchange.model.Exchange;

import java.util.List;
import java.util.Map;

@Service
public class ExchangeRateService {

    @Setter
    @Getter
    private List<Exchange> rates;

    private Double getRateByCurrency(Currency currency) {
        return rates.stream()
                .filter(rate -> rate.getCurrency().equals(currency))
                .findFirst()
                .map(Exchange::getValue)
                .orElseThrow(() -> new BadRequestException("Нет такой валюты."));
    }

    public Double convert(Currency from, Currency to, Double amount) {
        Double fromRate = getRateByCurrency(from);
        Double toRate = getRateByCurrency(to);
        return amount * fromRate / toRate;
    }

}
