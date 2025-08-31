package ru.yandex.transfer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ExchangeResponse {
    private Currency currency;
    private Double amount;
}
