package ru.yandex.transfer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TransferRequestDto {
    private TransferRequest transferRequest;
    private String token;
}
