package ru.yandex.account.service;

import io.micrometer.tracing.Tracer;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.account.dao.UserRepository;
import ru.yandex.account.model.User;
import ru.yandex.account.model.dto.EditUserInfoRequest;
import ru.yandex.account.model.dto.UserDto;

import java.nio.CharBuffer;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final NotificationService notificationService;

    private final PasswordEncoder passwordEncoder;

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final Tracer tracer;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRoles())
                .build();
    }

    public User findUserById(Long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    public User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> {
                    ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
                    ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
                    log.error("There is no user in security context.");
                    ThreadContext.clearAll();
                    return new UsernameNotFoundException("Пользователь не найден");
                });
    }

    public UserDto getCurrentUserDto() {
        User user = getCurrentUser();
        return new UserDto(user.getId(), user.getUsername(), user.getFullName(), user.getBirthday());
    }

    public List<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserDto(user.getId(), user.getUsername(), user.getFullName(), user.getBirthday()))
                .toList();
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public void save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        notificationService.send("Пользователь создан.");
        ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
        ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
        log.info("User: " + user.getUsername() + " created.");
        ThreadContext.clearAll();
    }

    public void deleteCurrentUser(){
        User user = getCurrentUser();
        userRepository.delete(user);
        notificationService.send("Пользователь удален.");
        ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
        ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
        log.info("User: " + user.getUsername() + " deleted.");
        ThreadContext.clearAll();
    }

    public void editPassword(String password){
        User user = getCurrentUser();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        notificationService.send("Пароль изменен.");
        ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
        ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
        log.info("User " + user.getUsername() + " changes password.");
        ThreadContext.clearAll();
    }

    public void editUser(EditUserInfoRequest userInfoDto){
        User user = getCurrentUser();
        user.setFullName(userInfoDto.getFullName());
        if (Period.between(userInfoDto.getBirthday(), LocalDate.now()).getYears() < 18){
            notificationService.send("Ошибка обновления данных: возраст меньше 18 лет.");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Возраст меньше 18 лет");
        }
        user.setBirthday(userInfoDto.getBirthday());
        userRepository.save(user);
        notificationService.send("Данные изменены.");
        ThreadContext.put("traceId", tracer.currentSpan().context().traceId());
        ThreadContext.put("spanId", tracer.currentSpan().context().spanId());
        log.info("User: " + user.getUsername() + " changes info.");
        ThreadContext.clearAll();
    }
}
