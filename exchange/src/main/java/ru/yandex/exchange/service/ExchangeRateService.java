package ru.yandex.exchange.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import ru.yandex.exchange.model.Currency;

import java.util.List;
import java.util.Map;

@Service
public class ExchangeRateService {

    @Setter
    @Getter
    private Map<Currency, Double> rate;

    private Double getRateByCurrency(Currency currency) {
        return rate.getOrDefault(currency, null);
    }

    public Double convert(Currency from, Currency to, Double amount) {
        Double fromRate = getRateByCurrency(from);
        Double toRate = getRateByCurrency(to);
        return amount * fromRate / toRate;
    }

}
