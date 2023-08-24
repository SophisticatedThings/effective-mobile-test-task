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
    id       serial
        primary key,
    username varchar(255)
);

alter table users
    owner to admin;

create table friends
(
    friend_id  integer not null
        constraint fkc42eihjtiryeriy8axlkpejo7
            references users,
    friend2_id integer not null
        constraint fkkjq3mk41pt4dal5ecugj0s8k7
            references users,
    primary key (friend_id, friend2_id)
);

alter table friends
    owner to admin;

create table friendships_requests
(
    receiver_id integer not null
        constraint fkfmdpgrmgogesti7dy7by0xvdw
            references users,
    sender_id   integer not null
        constraint fk6b0v5a4non6rkkt376xpa5gvl
            references users,
    primary key (receiver_id, sender_id)
);

alter table friendships_requests
    owner to admin;

create table subscriptions
(
    subscriber_id integer not null
        constraint fkoodc4352epkjrvxx79odlxbji
            references users,
    author_id     integer not null
        constraint fk4bxo233kaf6qxfj4g4jp55bnx
            references users,
    primary key (subscriber_id, author_id)
);

alter table subscriptions
    owner to admin;

