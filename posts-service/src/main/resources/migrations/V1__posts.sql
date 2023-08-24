create table posts
(
    id      serial
        primary key,
    title   varchar(255),
    content varchar,
    username varchar(255),
    created_at timestamp
);

alter table posts
    owner to admin;

create table images
(
    id        serial
        primary key,
    image_url varchar(255)
);

alter table images
    owner to admin;

create table posts_images
(
    posts_id  integer not null
        references posts
            on update cascade on delete cascade,
    images_id integer not null
        references images
            on update cascade,
    primary key (posts_id, images_id)
);

alter table posts_images
    owner to admin;

