package ru.yandex.transfer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ExternalTransferRequest {

    private Long userId;
    private Long fromAccountId;
    private Currency toCurrency;
    private Double amount;

    @Override
    public boolean equals(Object other){
        if (other.getClass() != ExternalTransferRequest.class){
            return false;
        }
        ExternalTransferRequest request = (ExternalTransferRequest) other;
        if (!Objects.equals(this.amount, request.amount)){
            return false;
        }
        if (!Objects.equals(this.userId, request.userId)){
            return false;
        }
        if(!Objects.equals(this.fromAccountId, request.fromAccountId)){
            return false;
        }
        return this.toCurrency.name().equals(request.toCurrency.name());
    }

}
