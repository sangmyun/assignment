<<<<<<< HEAD
CREATE TABLE IF NOT EXISTS members (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    login_id VARCHAR(20) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS schedules (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    plan_date DATE NOT NULL,
    content VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_schedules_member FOREIGN KEY (member_id) REFERENCES members (id) ON DELETE CASCADE
=======
create table if not exists members (
    id bigint auto_increment primary key,
    login_id varchar(20) not null unique,
    password varchar(255) not null,
    name varchar(30) not null,
    created_at datetime not null default current_timestamp,
    updated_at datetime null
);

create table if not exists schedules (
    id bigint auto_increment primary key,
    member_id bigint not null,
    plan_date date not null,
    content varchar(100) not null,
    created_at datetime not null default current_timestamp,
    updated_at datetime null,
    constraint fk_schedules_member foreign key (member_id) references members(id) on delete cascade
>>>>>>> 6926320 (nointercepter)
);
