package ru.yandex.front.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.front.TestSecurityConfig;

import java.util.Collection;
import java.util.List;

@SpringBootTest
@AutoConfigureStubRunner(
        ids = {
                "ru.yandex:account:+:stubs:8081",
                "ru.yandex:cash:+:stubs:8082",
                "ru.yandex:transfer:+:stubs:8083",
                "ru.yandex:exchange:+:stubs:8084"
        },
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
public abstract class BaseServiceStubIntegrationTest {

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(new Authentication() {
            @Override
            public String getName() {
                return "test-user";
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) {
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public Object getPrincipal() {
                return null;
            }

            @Override
            public Object getDetails() {
                return "dummy-token";
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of();
            }
        });
    }
}
