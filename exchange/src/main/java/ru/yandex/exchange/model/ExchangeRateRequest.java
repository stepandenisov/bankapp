package ru.yandex.exchange.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ExchangeRateRequest {

    private Map<Currency, Double> exchangeRate;

}
