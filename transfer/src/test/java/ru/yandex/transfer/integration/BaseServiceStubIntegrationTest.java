package ru.yandex.transfer.integration;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import ru.yandex.transfer.TestSecurityConfig;

import java.util.Collection;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureStubRunner(
        ids = {
                "ru.yandex:blocker:+:stubs:8081",
                "ru.yandex:account:+:stubs:8083",
                "ru.yandex:exchange:+:stubs:8084"
        },
        stubsMode = StubRunnerProperties.StubsMode.LOCAL
)
@Import(TestSecurityConfig.class)
public abstract class BaseServiceStubIntegrationTest {
}
