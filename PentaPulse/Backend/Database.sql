create database licenta;

create table users (
	id_user int not null auto_increment primary key ,
	nume varchar(16),
	prenume varchar(16),
	email varchar(255) ,
	nr_tel varchar(20),
	parola varchar(32));
create table aut_token(
	id_user numeric(2),
    data_login date,
    data_out time,
    foreign key (id_user) references users (id_user));
create table rec_traseu (
	id_traseu int not null auto_increment primary key,
    id_user int,
    nume_traseu varchar(16),
    locatie float,
    start_loc varchar(25),
    finish_loc varchar(25),
    foreign key (id_user) references users(id_user)); 
create table caracteristici (
	id_caracteristici numeric(2),
    id_traseu numeric(2),
    durata_traseu time,
    niv_dif varchar(16),
    varsta numeric(2),
    inaltime double,
    greutate double,
    foreign key (id_traseu) references traseu(id_traseu));
create table ruta(
    id_poz int not null auto_increment primary key,
    id_traseu int,
    nr_ord_poz_traseu int,
    lat float,
    lng float
);
select * from users;
