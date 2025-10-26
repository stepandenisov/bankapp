package ru.yandex.account.unit.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.account.controller.UserController;
import ru.yandex.account.model.dto.EditPasswordRequest;
import ru.yandex.account.model.dto.EditUserInfoRequest;
import ru.yandex.account.model.dto.UserDto;
import ru.yandex.account.service.UserService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerUnitTest {

    private UserService userService;
    private UserController userController;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        userController = new UserController(userService);
    }

    @Test
    void testGetUserInfo() {
        UserDto userDto = new UserDto(1L, "user", "Full Name", LocalDate.of(2000,1,1));
        when(userService.getCurrentUserDto()).thenReturn(userDto);

        ResponseEntity<UserDto> response = userController.getUserInfo();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userDto, response.getBody());
    }

    @Test
    void testGetUsers() {
        List<UserDto> users = List.of(
                new UserDto(1L, "user1", "Name1", LocalDate.of(2000,1,1)),
                new UserDto(2L, "user2", "Name2", LocalDate.of(2001,2,2))
        );
        when(userService.getUsers()).thenReturn(users);

        ResponseEntity<List<UserDto>> response = userController.getUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody());
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userService).deleteCurrentUser();

        ResponseEntity<?> response = userController.deleteUser();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).deleteCurrentUser();
    }

    @Test
    void testEditPassword_Success() {
        EditPasswordRequest request = new EditPasswordRequest("newpass", "newpass");

        doNothing().when(userService).editPassword("newpass");

        ResponseEntity<?> response = userController.editPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).editPassword("newpass");
    }

    @Test
    void testEditPassword_Failure_PasswordsMismatch() {
        EditPasswordRequest request = new EditPasswordRequest("pass1", "pass2");

        ResponseEntity<?> response = userController.editPassword(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Пароли не совпадают", response.getBody());
        verify(userService, never()).editPassword(any());
    }

    @Test
    void testEditUserInfo() {
        EditUserInfoRequest request = new EditUserInfoRequest("New Name", LocalDate.of(2000,1,1));

        doNothing().when(userService).editUser(request);

        ResponseEntity<?> response = userController.editUserInfo(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).editUser(request);
    }
}
