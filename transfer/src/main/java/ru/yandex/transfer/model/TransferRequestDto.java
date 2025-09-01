package ru.yandex.transfer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class TransferRequestDto {
    private ExternalTransferRequest transferRequest;
    private String token;
}
