package ru.yandex.account.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
@NoArgsConstructor
public class EditPasswordRequest {
    @NotBlank(message = "Пароль не может быть пустым")
    private String password;
    @JsonProperty("confirm_password")
    private String confirmPassword;
}
