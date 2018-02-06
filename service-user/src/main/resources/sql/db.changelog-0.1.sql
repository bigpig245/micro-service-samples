--liquibase formatted sql

--changeset bigpig:1
create sequence service_user.seq_user start 1000 increment 1;
create table service_user.user(id int8 not null, login varchar(255) not null, first_name varchar(255), last_name varchar(255), display_name varchar(510), email varchar(255) not null, password varchar(1000) not null, activation_token varchar(255), activation_expired_date timestamp, active boolean, version int4 not null, last_modified_date timestamp not null, created_date timestamp not null, primary key(id));
alter table service_user.user add constraint UQ_LOGIN unique (login);
alter table service_user.user add constraint UQ_EMAIL unique (email);