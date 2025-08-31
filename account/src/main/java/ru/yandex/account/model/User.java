package ru.yandex.account.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User {
    @Id
    @Getter
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Getter
    private String username;
    @Getter
    @Setter
    private String password;
    @Getter
    @Setter
    @Column(name = "full_name")
    private String fullName;

    @Getter
    @Setter
    private LocalDate birthday;

    private String roles;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts;

    public String[] getRoles(){
        return roles.split(",");
    }

}
