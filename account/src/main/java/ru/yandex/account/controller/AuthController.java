package ru.yandex.account.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.yandex.account.model.dto.RegisterRequest;
import ru.yandex.account.model.User;
import ru.yandex.account.model.dto.LoginRequest;
import ru.yandex.account.model.dto.TokenResponse;
import ru.yandex.account.service.JwtService;
import ru.yandex.account.service.UserService;

import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.createToken(userDetails);

            return ResponseEntity.ok(new TokenResponse(token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неверное имя пользователя или пароль");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Пользователь с таким логином уже существует");
        }

        if(!Objects.equals(request.getPassword(), request.getConfirmPassword())){
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Пароли не совпадают");
        }

        User user = new User(null,
                request.getUsername(),
                request.getPassword(),
                request.getFullName(),
                request.getBirthday(),
                "USER",
                null);

        userService.save(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("Пользователь успешно зарегистрирован");
    }
}
