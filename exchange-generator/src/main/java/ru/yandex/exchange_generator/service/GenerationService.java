package ru.yandex.exchange_generator.service;

import org.springframework.stereotype.Service;
import ru.yandex.exchange_generator.model.Currency;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class GenerationService {

    private final Map<Currency, List<Double>> ranges = Map.of(
            Currency.RUB, List.of(1.0, 1.0),
            Currency.USD, List.of(78.0, 82.0),
            Currency.CNY, List.of(10.0, 13.0)
    );

    public Double generateForCurrency(Currency currency){
        List<Double> range = ranges.get(currency);
        try {
            return ThreadLocalRandom.current().nextDouble(range.get(0), range.get(1));
        } catch (Exception e){
            return range.get(0);
        }
    }

}
