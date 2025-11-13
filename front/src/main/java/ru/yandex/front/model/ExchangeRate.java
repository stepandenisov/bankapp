package ru.yandex.front.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@EqualsAndHashCode
public class ExchangeRate {
    private Currency currency;
    private Double value;
}
