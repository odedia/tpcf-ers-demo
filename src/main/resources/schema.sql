create table if not exists attendee (
    id bigint auto_increment primary key,
    first_name varchar(255) not null,
    last_name varchar(255),
    address varchar(255),
    city varchar(255),
    state varchar(255),
    zip_code varchar(255),
    phone_number varchar(255),
    email_address varchar(255)
);
