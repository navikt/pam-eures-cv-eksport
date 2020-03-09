create table cv
(
    id             varchar(128) not null primary key,
    opprettet_ts   timestamp    not null,
    sist_endret_ts timestamp    not null,
    json_cv        text         not null
);
