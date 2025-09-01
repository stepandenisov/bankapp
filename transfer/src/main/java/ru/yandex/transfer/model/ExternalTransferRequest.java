package ru.yandex.transfer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ExternalTransferRequest {

    private Long userId;
    private Long fromAccountId;
    private Currency toCurrency;
    private Double amount;

}
