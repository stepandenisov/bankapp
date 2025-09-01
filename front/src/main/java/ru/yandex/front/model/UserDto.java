package ru.yandex.front.model;

import java.time.LocalDate;

public record UserDto(Long id, String username, String fullName, LocalDate birthday){};
