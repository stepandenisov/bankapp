package ru.yandex.transfer.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Account {

    private Long id;

    private Currency currency;

    @Setter
    private Double reminder;
}
