create table ne.notification_component_status
(
    status      varchar(24) unique not null,
    description varchar(256)       not null,
    constraint status_pk
        primary key (status)
);

--
-- Describes the manner by which the content for a notification subject or body should be obtained
-- Notification Engine should allow managing its own content, without features, outside the context of Content Mgr
-- Also allow for URL
create table ne.content_lookup_types
(
    type        varchar(56) unique not null,
    description varchar(256)       not null,
    constraint content_lookup_types_pk
        primary key (type)
);

-- In systems not using Content Manager, Notification Engine can store its own content
create table ne.basic_static_content
(
    content_key     varchar(50) UNIQUE not null,
    lookup_type_key varchar(15000)     not null,
    constraint basic_static_content_pk
        primary key (content_key)
);
-- When content is saved, merge fields are parsed out and made available as metadata
create table ne.content_merge_fields
(
    content_key      varchar(50)  not null,
    merge_field_name varchar(250) not null
);

--
-- Content type URL: key = URL
-- Content type CONTEXT_KEY: Use CMS
-- Content type STATIC: key refers to basic_static_content
-- recipient_context_key - context value holding the value to be used as recipient (or prefix with two underscores,
-- indicating a constant)
create table ne.message_templates
(
    id                          BIGINT auto_increment,
    tenant_id                   BIGINT                               not null,
    event_subject               varchar(50)                          null,
    event_verb                  varchar(50)                          null,
    status                      varchar(1)                           not null,
    recipient_context_key       varchar(128)                         null,
    summary_content_lookup_type varchar(56)                          NOT NULL,
    summary_content_key         varchar(50)                          not null,
    body_content_lookup_type    varchar(56)                          NOT NULL,
    body_content_key            varchar(50)                          not null,
    created_date                datetime DEFAULT CURRENT_TIMESTAMP   not null,
    modified_date               datetime ON UPDATE CURRENT_TIMESTAMP null,
    constraint message_templates_pk
        primary key (id)
);

--
-- Key value definition of criteria to be met by message context for a template to result in a delivered notification
create table ne.delivery_criteria
(
    id                BIGINT auto_increment,
    message_template_id BIGINT       not null,
    context_key       varchar(100) not null,
    value             varchar(100) not null,
    constraint delivery_criteria_pk
        primary key (id)
);

create table ne.delivered_notifications
(
    id                  BIGINT auto_increment              not null,
    recipient           varchar(255)                       not null,
    message_template_id BIGINT                             not null,
    created_date        datetime DEFAULT CURRENT_TIMESTAMP not null,
    constraint delivered_notifications_pk
        primary key (id),
    constraint delivered_notifications_messages_template_fk
        FOREIGN KEY (message_template_id) references message_templates (id)
);

--
-- Those context keys used in the building of the context for a notification should be logged as metadata
create table ne.delivered_notifications_metadata
(
    id                        BIGINT auto_increment              not null,
    delivered_notification_id BIGINT                             not null,
    metadata_key              varchar(255)                       not null,
    metadata_value            varchar(255)                       not null,
    created_date              datetime DEFAULT CURRENT_TIMESTAMP not null,
    constraint delivered_notifications_pk
        primary key (id),
    constraint delivered_notifications_metadata_delivered_notifications_fk
        FOREIGN KEY (delivered_notification_id) references delivered_notifications (id)
);

--
-- Services that the context builder can call to build a context for merge data for a notification
-- Dynamic data is denoted in the URL by {{}}, the context key for which is indicated in context_service_param_context_key
create table ne.context_services
(
    id            BIGINT auto_increment,
    tenant_id     BIGINT                             not null,
    service_url   varchar(1024)                      not null,
    num_retries   BIGINT                             null,
    created_date  datetime DEFAULT CURRENT_TIMESTAMP not null,
    modified_date datetime ON UPDATE CURRENT_TIMESTAMP,
    constraint context_services_pk
        primary key (id)
);

--
-- For a message template using a context service, specify the context key from which the value for each param can be gleaned
create table ne.context_service_param_context_key
(
    id                 BIGINT auto_increment,
    context_service_id BIGINT                             not null,
    param_name         varchar(50)                        not null,
    context_key        varchar(50)                        not null,
    created_date       datetime DEFAULT CURRENT_TIMESTAMP not null,
    modified_date      datetime ON UPDATE CURRENT_TIMESTAMP,
    constraint context_services_pk
        primary key (id),
    constraint context_service_param_context_key_context_services_fk
        FOREIGN KEY (context_service_id) references context_services (id)
);


