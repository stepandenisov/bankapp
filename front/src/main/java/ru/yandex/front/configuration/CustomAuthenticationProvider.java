package ru.yandex.front.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import ru.yandex.front.model.LoginRequest;
import ru.yandex.front.model.TokenResponse;

import java.util.List;

@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final RestTemplate restTemplate;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        LoginRequest loginRequest = new LoginRequest(username, password);
        HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);

        try {
            ResponseEntity<TokenResponse> response = restTemplate.postForEntity(
                    "http://account/auth/login", request, TokenResponse.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, null, List.of());
                token.setDetails(response.getBody().getAccessToken());
                return token;
            } else {
                throw new BadCredentialsException("Invalid credentials");
            }

        } catch (HttpClientErrorException e) {
            throw new BadCredentialsException("Login failed: " + e.getMessage());
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
