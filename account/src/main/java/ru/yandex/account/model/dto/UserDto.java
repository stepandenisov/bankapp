package ru.yandex.account.model.dto;

import java.time.LocalDate;

public record UserDto (Long id, String username, String fullName, LocalDate birthday){};
