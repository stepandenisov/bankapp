package ru.yandex.account.unit.service;

import io.micrometer.tracing.Tracer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.account.dao.UserRepository;
import ru.yandex.account.model.User;
import ru.yandex.account.model.dto.EditUserInfoRequest;
import ru.yandex.account.model.dto.UserDto;
import ru.yandex.account.service.NotificationService;
import ru.yandex.account.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceUnitTest {

    private UserRepository userRepository;
    private NotificationService notificationService;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    private User user;

    private Tracer tracer;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        notificationService = mock(NotificationService.class);
        passwordEncoder = mock(PasswordEncoder.class);
        tracer = mock(Tracer.class);
        when(tracer.currentSpan().context().traceId()).thenReturn("");
        when(tracer.currentSpan().context().spanId()).thenReturn("");
        userService = new UserService(userRepository, notificationService, passwordEncoder, tracer);

        user = new User(1L, "user", "pass", "Full Name", LocalDate.of(2000, 1, 1), "USER", null);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(
                org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .roles(user.getRoles())
                        .build()
        );

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
    }

    @Test
    void testLoadUserByUsername_Success() {
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));

        var userDetails = userService.loadUserByUsername("user");

        assertEquals("user", userDetails.getUsername());
        assertEquals("pass", userDetails.getPassword());
    }

    @Test
    void testLoadUserByUsername_NotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> userService.loadUserByUsername("unknown"));
    }

    @Test
    void testGetCurrentUser() {
        User current = userService.getCurrentUser();
        assertEquals(user, current);
    }

    @Test
    void testGetCurrentUserDto() {
        UserDto dto = userService.getCurrentUserDto();
        assertEquals(user.getId(), dto.id());
        assertEquals(user.getUsername(), dto.username());
        assertEquals(user.getFullName(), dto.fullName());
        assertEquals(user.getBirthday(), dto.birthday());
    }

    @Test
    void testGetUsers() {
        User user2 = new User(2L, "user2", "pass2", "Name2", LocalDate.of(1990, 1, 1), "USER", null);
        when(userRepository.findAll()).thenReturn(List.of(user, user2));

        List<UserDto> users = userService.getUsers();

        assertEquals(2, users.size());
        assertEquals("user", users.get(0).username());
        assertEquals("user2", users.get(1).username());
    }

    @Test
    void testExistsByUsername() {
        when(userRepository.existsByUsername("user")).thenReturn(true);

        assertTrue(userService.existsByUsername("user"));
    }

    @Test
    void testSave() {
        when(passwordEncoder.encode("pass")).thenReturn("encodedPass");

        userService.save(user);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        verify(notificationService).send("Пользователь создан.");

        assertEquals("encodedPass", captor.getValue().getPassword());
    }

    @Test
    void testDeleteCurrentUser() {
        userService.deleteCurrentUser();

        verify(userRepository).delete(user);
        verify(notificationService).send("Пользователь удален.");
    }

    @Test
    void testEditPassword() {
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

        userService.editPassword("newPass");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        verify(notificationService).send("Пароль изменен.");

        assertEquals("encodedNewPass", captor.getValue().getPassword());
    }

    @Test
    void testEditUser_Success() {
        EditUserInfoRequest request = new EditUserInfoRequest("New Name", LocalDate.of(2000, 1, 1));
        userService.editUser(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        verify(notificationService).send("Данные изменены.");

        assertEquals("New Name", captor.getValue().getFullName());
    }

    @Test
    void testEditUser_Underage() {
        EditUserInfoRequest request = new EditUserInfoRequest("New Name", LocalDate.now().minusYears(17));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> userService.editUser(request));

        assertEquals("Возраст меньше 18 лет", exception.getReason());
        verify(notificationService).send("Ошибка обновления данных: возраст меньше 18 лет.");
        verify(userRepository, never()).save(any());
    }
}
