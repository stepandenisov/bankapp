package ru.yandex.exchange_generator.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Exchange {
    private Currency currency;
    private Double value;
}
