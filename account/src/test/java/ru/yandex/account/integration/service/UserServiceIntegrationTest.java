package ru.yandex.account.integration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.account.dao.UserRepository;
import ru.yandex.account.integration.BaseIntegrationTest;
import ru.yandex.account.model.dto.EditUserInfoRequest;

import jakarta.ws.rs.BadRequestException;
import ru.yandex.account.service.NotificationService;
import ru.yandex.account.service.UserService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.springframework.security.core.userdetails.User.withUsername;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@WithMockUser(username = "testuser")
class UserServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private NotificationService notificationService;

    @Test
    void loadUserByUsername_shouldReturnUserDetails() {
        var userDetails = userService.loadUserByUsername("testuser");

        assertThat(userDetails.getUsername()).isEqualTo("testuser");
        assertThat(userDetails.getAuthorities()).extracting("authority")
                .contains("ROLE_USER");
    }

    @Test
    void getCurrentUser_shouldReturnUserFromSecurityContext() {
        var current = userService.getCurrentUser();

        assertThat(current.getUsername()).isEqualTo(testUser.getUsername());
    }

    @Test
    void save_shouldEncodePasswordAndSendNotification() {
        ru.yandex.account.model.User newUser = new ru.yandex.account.model.User();
        newUser.setUsername("newuser");
        newUser.setPassword("rawpass");
        newUser.setFullName("New User");
        newUser.setRoles("USER");
        newUser.setBirthday(LocalDate.of(1995, 1, 1));

        userService.save(newUser);

        ru.yandex.account.model.User saved = userRepository.findByUsername("newuser").orElseThrow();
        assertThat(passwordEncoder.matches("rawpass", saved.getPassword())).isTrue();

        verify(notificationService).send("Пользователь создан.");
    }

    @Test
    void editPassword_shouldUpdatePasswordAndSendNotification() {
        userService.editPassword("newpass");

        ru.yandex.account.model.User updated = userRepository.findByUsername("testuser").orElseThrow();
        assertThat(passwordEncoder.matches("newpass", updated.getPassword())).isTrue();
        verify(notificationService).send("Пароль изменен.");
    }

    @Test
    void editUser_shouldUpdateUserInfo_whenAgeIsValid() {
        EditUserInfoRequest req = new EditUserInfoRequest();
        req.setFullName("Updated Name");
        req.setBirthday(LocalDate.of(2000, 1, 1));

        userService.editUser(req);

        ru.yandex.account.model.User updated = userRepository.findByUsername("testuser").orElseThrow();
        assertThat(updated.getFullName()).isEqualTo("Updated Name");
        assertThat(updated.getBirthday()).isEqualTo(req.getBirthday());
        verify(notificationService).send("Данные изменены.");
    }

    @Test
    void editUser_shouldThrowBadRequest_whenUserIsTooYoung() {
        EditUserInfoRequest req = new EditUserInfoRequest();
        req.setFullName("Too Young");
        req.setBirthday(LocalDate.now().minusYears(17));

        assertThatThrownBy(() -> userService.editUser(req))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Возраст меньше 18 лет");
    }

    @Test
    void deleteCurrentUser_shouldRemoveUserAndSendNotification() {
        userService.deleteCurrentUser();

        assertThat(userRepository.findByUsername("testuser")).isEmpty();
        verify(notificationService).send("Пользователь удален.");
    }

    @Test
    void getUsers_shouldReturnAllUsersAsDtos() {
        var users = userService.getUsers();

        assertThat(users).extracting("username").contains("testuser");
    }
}
