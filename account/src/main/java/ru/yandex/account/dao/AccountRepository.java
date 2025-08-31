package ru.yandex.account.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.account.model.Account;
import ru.yandex.account.model.AccountType;
import ru.yandex.account.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUser(User user);

    Optional<Account> findByIdAndUser(Long id, User user);

    boolean existsByUserIdAndAccountType(Long userId, AccountType type);
}
