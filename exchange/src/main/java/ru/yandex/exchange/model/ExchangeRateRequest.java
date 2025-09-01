package ru.yandex.exchange.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ExchangeRateRequest {

    private List<Exchange> exchangeRate;

}
