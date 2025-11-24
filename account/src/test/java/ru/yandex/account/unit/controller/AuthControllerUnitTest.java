package ru.yandex.account.unit.controller;

import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import ru.yandex.account.controller.AuthController;
import ru.yandex.account.model.dto.LoginRequest;
import ru.yandex.account.model.dto.RegisterRequest;
import ru.yandex.account.model.dto.TokenResponse;
import ru.yandex.account.service.AuthService;
import ru.yandex.account.service.JwtService;
import ru.yandex.account.service.UserService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerUnitTest {

    private AuthenticationManager authenticationManager;
    private JwtService jwtService;
    private UserService userService;
    private AuthController authController;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authenticationManager = mock(AuthenticationManager.class);
        jwtService = mock(JwtService.class);
        userService = mock(UserService.class);
        authService = mock(AuthService.class);
        authController = new AuthController(authenticationManager, jwtService, userService, authService);
    }

    @Test
    void testLogin_Success() {
        LoginRequest request = new LoginRequest("user", "pass");
        Authentication auth = mock(Authentication.class);
        UserDetails userDetails = User.withUsername("user").password("pass").roles("USER").build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(jwtService.createToken(userDetails)).thenReturn("token123");

        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof TokenResponse);
        assertEquals("token123", ((TokenResponse) response.getBody()).getAccessToken());
    }

    @Test
    void testLogin_Failure_InvalidCredentials() {
        LoginRequest request = new LoginRequest("user", "wrongpass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(mock(AuthenticationException.class));

        ResponseEntity<?> response = authController.login(request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Неверное имя пользователя или пароль", response.getBody());
    }

    @Test
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest(
                "newuser",
                "pass123",
                "pass123",
                "Full Name",
                LocalDate.of(2000, 1, 1)
        );

        when(userService.existsByUsername("newuser")).thenReturn(false);

        ResponseEntity<?> response = authController.register(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Пользователь успешно зарегистрирован", response.getBody());
        verify(userService, times(1)).save(any());
    }

    @Test
    void testRegister_Failure_UsernameExists() {
        RegisterRequest request = new RegisterRequest(
                "existinguser",
                "pass123",
                "pass123",
                "Full Name",
                LocalDate.of(2000, 1, 1)
        );

        when(userService.existsByUsername("existinguser")).thenReturn(true);

        ResponseEntity<?> response = authController.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Пользователь с таким логином уже существует", response.getBody());
        verify(userService, never()).save(any());
    }

    @Test
    void testRegister_Failure_PasswordsMismatch() {
        RegisterRequest request = new RegisterRequest(
                "newuser",
                "pass123",
                "pass321",
                "Full Name",
                LocalDate.of(2000, 1, 1)
        );

        when(userService.existsByUsername("newuser")).thenReturn(false);

        ResponseEntity<?> response = authController.register(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Пароли не совпадают", response.getBody());
        verify(userService, never()).save(any());
    }
}
