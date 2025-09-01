package ru.yandex.account.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "accounts")
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Setter
    private Double reminder;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
