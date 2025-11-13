package ru.yandex.front.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ExchangeRateResponse {
    private List<ExchangeRate> rate;
}
