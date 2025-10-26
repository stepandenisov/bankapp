package ru.yandex.account.integration;

import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.yandex.account.configuration.TestSecurityConfig;
import ru.yandex.account.dao.AccountRepository;
import ru.yandex.account.dao.UserRepository;
import ru.yandex.account.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public abstract class BaseIntegrationTest {

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean
    private OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository;

    protected User testUser;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;


    @BeforeEach
    void setUp() {
        testUser = userRepository.findByUsername("testuser").orElseGet(() -> {
            User u = new User();
            u.setUsername("testuser");
            u.setPassword("password");
            u.setFullName("Test User");
            u.setBirthday(LocalDate.of(1990, 1, 1));
            u.setRoles("USER");
            return userRepository.save(u);
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

    @AfterEach
    void clean(){
        accountRepository.deleteAll();
    }
}
