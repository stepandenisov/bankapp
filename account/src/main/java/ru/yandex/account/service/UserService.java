package ru.yandex.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.yandex.account.dao.UserRepository;
import ru.yandex.account.model.User;
import ru.yandex.account.model.dto.EditUserInfoRequest;
import ru.yandex.account.model.dto.UserDto;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final NotificationService notificationService;

    private final PasswordEncoder passwordEncoder;

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
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
    }

    public UserDto getCurrentUserDto() {
        User user = getCurrentUser();
        return new UserDto(user.getUsername(), user.getFullName(), user.getBirthday());
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public void save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        notificationService.send("Пользователь создан.");
    }

    public void deleteCurrentUser(){
        userRepository.delete(getCurrentUser());
        notificationService.send("Пользователь удален.");
    }

    public void editPassword(String password){
        User user = getCurrentUser();
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        notificationService.send("Пароль изменен.");
    }

    public void editUser(EditUserInfoRequest userInfoDto){
        User user = getCurrentUser();
        user.setFullName(userInfoDto.getFullName());
        user.setBirthday(userInfoDto.getBirthday());
        userRepository.save(user);
        notificationService.send("Данные изменены.");
    }
}
