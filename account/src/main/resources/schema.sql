create schema if not exists account;
drop table if exists account.accounts;
drop table if exists account.users;
create table if not exists account.users
(
    id       bigserial primary key,
    full_name varchar(256) not null,
    password varchar(256) not null,
    username    varchar(256) not null unique,
    birthday    date not null,
    roles    varchar(256) not null
);
create table if not exists account.accounts
(
    id bigserial primary key,
    account_type varchar(64) check (accounts.account_type in ('RUB', 'USD', 'CNY')) not null,
    user_id bigint not null references account.users(id),
    reminder numeric not null,
    constraint unique_user_account_type unique (user_id, account_type)
)
