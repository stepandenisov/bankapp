package ru.yandex.front.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ExternalTransferRequest {

    private Long userId;
    private Long fromAccountId;
    private Currency toCurrency;
    private Double amount;

}
