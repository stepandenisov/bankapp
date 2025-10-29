package ru.yandex.account.contract;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.account.configuration.TestSecurityConfig;
import ru.yandex.account.dao.AccountRepository;
import ru.yandex.account.dao.UserRepository;
import ru.yandex.account.model.Account;
import ru.yandex.account.model.Currency;
import ru.yandex.account.model.User;

import java.time.LocalDate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMessageVerifier
@AutoConfigureMockMvc
@Import(StubSecurityConfig.class)
public abstract class BaseContractTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected MockMvc mockMvc;

    @BeforeEach
    public void setupRestAssuredMockMvc() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;


    @BeforeEach
    void setUp() {
//        accountRepository.deleteAll();
//        accountRepository.flush();
//        userRepository.deleteAll();
//        userRepository.flush();

        User testUser = userRepository.findByUsername("test").orElseGet(() -> {
            User u = new User();
            u.setUsername("test");
            u.setPassword("$2a$10$i2kBQjDZva/v5mNjpOhvfetEGmLcXSX2RknrGgYy1zHUprW532NUC");
            u.setFullName("Test User");
            u.setBirthday(LocalDate.of(1990, 1, 1));
            u.setRoles("USER");
            return userRepository.save(u);
        });

        accountRepository.findById(1L).orElseGet(() -> {
            Account a = new Account();
            a.setReminder(1000.0);
            a.setCurrency(Currency.USD);
//            a.setId(1L);
            a.setUser(testUser);
            return accountRepository.saveAndFlush(a);
        });

        accountRepository.findById(2L).orElseGet(() -> {
            Account a = new Account();
            a.setReminder(1000.0);
            a.setCurrency(Currency.RUB);
//            a.setId(1L);
            a.setUser(testUser);
            return accountRepository.saveAndFlush(a);
        });

        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(testUser.getUsername())
                .password(testUser.getPassword())
                .roles(testUser.getRoles())
                .build();

        TestingAuthenticationToken auth =
                new TestingAuthenticationToken(userDetails, "password", "USER");
        SecurityContextHolder.getContext().setAuthentication(auth);

    }
}
