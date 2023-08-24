create sequence users_seq
    increment by 50;

alter sequence users_seq owner to admin;

create table flyway_schema_history
(
    installed_rank integer                 not null
        constraint flyway_schema_history_pk
            primary key,
    version        varchar(50),
    description    varchar(200)            not null,
    type           varchar(20)             not null,
    script         varchar(1000)           not null,
    checksum       integer,
    installed_by   varchar(100)            not null,
    installed_on   timestamp default now() not null,
    execution_time integer                 not null,
    success        boolean                 not null
);

alter table flyway_schema_history
    owner to admin;

create index flyway_schema_history_s_idx
    on flyway_schema_history (success);

create table users
(
    id       integer not null
        primary key,
    email    varchar(255),
    password varchar(255),
    role     varchar(255),
    username varchar(255)
);

alter table users
    owner to admin;

