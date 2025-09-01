package ru.yandex.transfer.model;

import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SelfTransferRequest {

    private Long fromAccountId;
    private Long toAccountId;
    private Double amount;

}
