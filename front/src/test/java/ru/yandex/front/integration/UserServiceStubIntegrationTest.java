package ru.yandex.front.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.front.model.EditPasswordRequest;
import ru.yandex.front.model.EditUserInfoRequest;
import ru.yandex.front.model.RegisterRequest;
import ru.yandex.front.model.UserDto;
import ru.yandex.front.service.UserService;

import java.time.LocalDate;
import java.util.List;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class UserServiceStubIntegrationTest extends BaseServiceStubIntegrationTest{

    @Autowired
    private UserService userService;

    @Test
    void userService_register_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password");

        userService.register(request);
    }

    @Test
    void userService_editInfo_Success() {
        EditUserInfoRequest request = new EditUserInfoRequest();
        request.setFullName("test");
        request.setBirthday(LocalDate.of(1990, 1, 1));
        userService.editInfo(request);
    }

    @Test
    void userService_editPassword_Success() {
        EditPasswordRequest request = new EditPasswordRequest();
        request.setPassword("newpass");
        request.setConfirmPassword("newpass");

        userService.editPassword(request);
    }

    @Test
    void userService_getUsers_ReturnsList() {
        List<UserDto> users = userService.getUsers();
        assertNotNull(users);
        assertFalse(users.isEmpty());
    }

}
