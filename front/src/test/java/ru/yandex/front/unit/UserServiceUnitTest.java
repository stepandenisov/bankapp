package ru.yandex.front.unit;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.yandex.front.model.EditPasswordRequest;
import ru.yandex.front.model.EditUserInfoRequest;
import ru.yandex.front.model.UserDto;
import ru.yandex.front.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceUnitTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private CircuitBreakerRegistry cbRegistry;

    @Mock
    private CircuitBreaker circuitBreaker;

    @Mock
    private RetryRegistry retryRegistry;

    @Mock
    private Retry retry;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Authentication auth = mock(Authentication.class);
        when(auth.getDetails()).thenReturn("dummy-token");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);

        when(cbRegistry.circuitBreaker(anyString())).thenReturn(circuitBreaker);
        when(circuitBreaker.decorateSupplier(any(Supplier.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(retryRegistry.retry(anyString())).thenReturn(retry);
    }

    @Test
    void editInfo_shouldCallRestTemplate() {
        doReturn(new ResponseEntity<Void>(HttpStatus.OK))
                .when(restTemplate).exchange(
                        contains("/user/info"),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(Void.class)
                );

        userService.editInfo(new EditUserInfoRequest("new", LocalDate.of(1990, 1, 1)));

        verify(restTemplate, times(1)).exchange(
                contains("/user/info"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }

    @Test
    void editPassword_shouldCallRestTemplate() {
        doReturn(new ResponseEntity<Void>(HttpStatus.OK))
                .when(restTemplate).exchange(
                        contains("/user/password"),
                        eq(HttpMethod.POST),
                        any(HttpEntity.class),
                        eq(Void.class)
                );

        userService.editPassword(new EditPasswordRequest("newPassword", "newPassword"));

        verify(restTemplate, times(1)).exchange(
                contains("/user/password"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }
}
