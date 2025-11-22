package ru.yandex.account.service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.yandex.account.model.User;
import ru.yandex.account.model.dto.LoginRequest;
import ru.yandex.account.model.dto.RegisterRequest;
import ru.yandex.account.model.dto.TokenResponse;

import java.util.Objects;


@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private final UserService userService;
    private final MeterRegistry registry;


    public TokenResponse login(LoginRequest request){
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.createToken(userDetails);
            registry.counter("auth_login", "username", request.getUsername(), "status", "success").increment();
            return new TokenResponse(token);
        } catch (AuthenticationException e) {
            registry.counter("auth_login", "username", request.getUsername(), "status", "fail").increment();
            throw new RuntimeException();
        }
    }

    public boolean register(RegisterRequest request){
        if (userService.existsByUsername(request.getUsername())) {
            return false;
        }

        if(!Objects.equals(request.getPassword(), request.getConfirmPassword())){
            return false;
        }

        User user = new User(null,
                request.getUsername(),
                request.getPassword(),
                request.getFullName(),
                request.getBirthday(),
                "USER",
                null);

        userService.save(user);

        return true;
    }

}
