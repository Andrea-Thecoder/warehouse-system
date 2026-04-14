-- apply changes
create table role_type (
  id                            varchar(255) not null,
  label                         varchar(255) not null,
  description                   varchar(255) not null,
  constraint pk_role_type primary key (id)
);

create table user_registration (
  id                            uuid not null,
  _version                      bigint not null,
  _data_creazione               timestamptz not null,
  _data_modifica                timestamptz not null,
  keycloak_user_id              varchar(255),
  status                        varchar(16) not null,
  _utente_creazione             varchar(255) not null,
  _utente_modifica              varchar(255) not null,
  constraint ck_user_registration_status check ( status in ('PENDING','APPROVED','PARTIAL_APPROVED','REJECTED')),
  constraint uq_user_registration_keycloak_user_id unique (keycloak_user_id),
  constraint pk_user_registration primary key (id)
);

create table registration_request_role (
  registration_request_id       uuid not null,
  role_id                       varchar(255) not null,
  constraint pk_registration_request_role primary key (registration_request_id,role_id)
);

-- foreign keys and indices
create index ix_registration_request_role_user_registration on registration_request_role (registration_request_id);
alter table registration_request_role add constraint fk_registration_request_role_user_registration foreign key (registration_request_id) references user_registration (id) on delete restrict on update restrict;

create index ix_registration_request_role_role_type on registration_request_role (role_id);
alter table registration_request_role add constraint fk_registration_request_role_role_type foreign key (role_id) references role_type (id) on delete restrict on update restrict;

