package ru.yandex.front.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SelfTransferRequest {

    private Long fromAccountId;
    private Long toAccountId;
    private Double amount;

}
