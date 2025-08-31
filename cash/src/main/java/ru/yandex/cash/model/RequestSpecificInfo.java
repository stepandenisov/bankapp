package ru.yandex.cash.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RequestSpecificInfo {
    private Long accountId;
    private CashRequest cashRequest;
    private String token;
}
