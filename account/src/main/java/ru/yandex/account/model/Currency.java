package ru.yandex.account.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Currency {
    RUB,
    USD,
    CNY;

    @JsonCreator
    public static Currency fromString(String value) {
        return Currency.valueOf(value.toUpperCase());
    }
}
