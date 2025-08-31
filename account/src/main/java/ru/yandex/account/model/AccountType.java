package ru.yandex.account.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum AccountType {
    RUB,
    USD,
    CNY;

    @JsonCreator
    public static AccountType fromString(String value) {
        return AccountType.valueOf(value.toUpperCase());
    }
}
