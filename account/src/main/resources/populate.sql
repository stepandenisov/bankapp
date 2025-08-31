insert into account.users(username, password, full_name, birthday, roles)
values ('user', '$2a$12$OJzc087Soz9T1crkIgF6e.uktwqXAvLIerUV5.NU0Ak9vdsEnW6Ei', 'Иван', DATE ('2000-01-01'), 'USER'),
       ('user2', '$2a$12$OJzc087Soz9T1crkIgF6e.uktwqXAvLIerUV5.NU0Ak9vdsEnW6Ei', 'Ольга', DATE ('1999-01-01'), 'USER');

insert into account.accounts(account_type, user_id, reminder)
values ('RUB'::account_type, 1, 1000.0),
       ('USD'::account_type, 1, 200.0),
        ('CNY'::account_type, 2, 500.0);