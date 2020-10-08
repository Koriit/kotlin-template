create table users (
    id identity not null primary key,
    login varchar(100) not null unique,
    name varchar(100) not null,
    age int not null,
    password_hash varchar(100) not null,
    last_update timestamp not null
);
