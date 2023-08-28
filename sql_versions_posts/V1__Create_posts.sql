create database posts;

create sequence images_seq
    increment by 50;

alter sequence images_seq owner to admin;

create sequence posts_seq
    increment by 50;

alter sequence posts_seq owner to admin;

create table images
(
    id integer not null primary key,
    image_url varchar(255)
);

alter table images
    owner to admin;

create table posts
(
    id integer not null primary key,
    content    varchar(255),
    created_at timestamp(6),
    title      varchar(255),
    username   varchar(255)
);

alter table posts
    owner to admin;

create table posts_images
(
    posts_id  integer not null
        constraint fkhw31vl3nn11b8bmm619aptbf7
            references posts,
    images_id integer not null
        constraint uk_fi79rtthvau8hx7k5q8lbwy6j
            unique
        constraint fk9q302vs6snt6haevai6ilj1if
            references images
);

alter table posts_images
    owner to admin;

