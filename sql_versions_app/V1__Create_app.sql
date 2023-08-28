create database app;

create sequence users_seq
    increment by 50;

alter sequence users_seq owner to admin;

create table users
(
    id integer not null primary key,
    email    varchar(255),
    password varchar(255),
    role     varchar(255),
    username varchar(255)
);

alter table users
    owner to admin;

