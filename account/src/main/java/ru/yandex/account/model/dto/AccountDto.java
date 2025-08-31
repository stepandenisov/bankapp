package ru.yandex.account.model.dto;

import ru.yandex.account.model.AccountType;


public record AccountDto(AccountType type, Double reminder) {
}