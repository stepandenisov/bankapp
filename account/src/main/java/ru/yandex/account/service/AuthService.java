package ru.yandex.account.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final Tracer tracer;


    public TokenResponse login(LoginRequest request){
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.createToken(userDetails);
            registry.counter("auth_login", "username", request.getUsername(), "status", "success").increment();
            ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
            ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
            log.debug("Login success: " + userDetails.getUsername());
            ThreadContext.clearAll();
            return new TokenResponse(token);
        } catch (AuthenticationException e) {
            ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
            ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
            log.info("Failed login for user: " + request.getUsername() + ".");
            ThreadContext.clearAll();
            registry.counter("auth_login", "username", request.getUsername(), "status", "fail").increment();
            throw e;
        }
    }

    public boolean register(RegisterRequest request){
        if (userService.existsByUsername(request.getUsername())) {
            ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
            ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
            log.debug("Register: username already exists.");
            ThreadContext.clearAll();
            return false;
        }

        if(!Objects.equals(request.getPassword(), request.getConfirmPassword())){
            ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
            ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
            log.debug("Register: passwords doesn't matches.");
            ThreadContext.clearAll();
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

        ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
        ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
        log.info("New user: " + user.getUsername());
        ThreadContext.clearAll();

        return true;
    }

}
