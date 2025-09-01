package ru.yandex.front.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class Account {

    private Long id;

    private Currency currency;

    private Double reminder;

}
