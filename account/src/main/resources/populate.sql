insert into account.users(username, password, full_name, birthday, roles)
values ('user', '$2a$12$OJzc087Soz9T1crkIgF6e.uktwqXAvLIerUV5.NU0Ak9vdsEnW6Ei', 'Иван', DATE ('2000-01-01'), 'USER'),
       ('user2', '$2a$12$OJzc087Soz9T1crkIgF6e.uktwqXAvLIerUV5.NU0Ak9vdsEnW6Ei', 'Ольга', DATE ('1999-01-01'), 'USER');

insert into account.accounts(currency, user_id, reminder)
values ('RUB', 1, 1000.0),
       ('USD', 1, 200.0),
        ('CNY', 2, 500.0);