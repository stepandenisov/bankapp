package ru.yandex.account.model.dto;

import java.time.LocalDate;


public record UserDto(String username, String fullName, LocalDate birthday) {
}
