package ru.yandex.cash.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class NotificationRequest {
    private String message;

    @Override
    public boolean equals(Object other){
        if (other instanceof NotificationRequest nr){
            return nr.getMessage().equals(this.message);
        }
        return false;
    }
}
