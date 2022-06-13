drop schema ne;
create schema ne;
use ne;

create table notification_component_status
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
create table content_lookup_types
(
    type        varchar(56) unique not null,
    description varchar(256)       not null,
    constraint content_lookup_types_pk
        primary key (type)
);

-- In systems not using Content Manager, Notification Engine can store its own content
-- TODO: CMS vs Static is flagged by tenant configuration. See https://solutionsmodular.atlassian.net/browse/NE-12
create table local_static_content
(
    id            BIGINT auto_increment,
    tenant_id     BIGINT,
    content_key   varchar(250) not null,
    content_block blob         not null,
    constraint local_static_content_pk
        primary key (id)
);
CREATE INDEX local_static_content_tenant_id_pk ON local_static_content (tenant_id);
CREATE INDEX local_static_content_key_pk ON local_static_content (content_key);

-- When content is saved, merge fields are parsed out and cataloged for ease of reference
create table content_merge_fields
(
    local_static_content_id BIGINT,
    content_key             varchar(50)  not null,
    merge_field_name        varchar(250) not null,
    constraint content_merge_fields_local_static_content_id_fk
        FOREIGN KEY (local_static_content_id) references local_static_content (id)
);

--
-- Content type URL: key = URL
-- Content type CONTEXT_KEY: Use CMS
-- Content type STATIC: key refers to local_static_content
-- recipient_context_key - context value holding the value to be used as recipient (or prefix with two underscores,
-- indicating a constant)
create table message_templates
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
        primary key (id),
    constraint message_templates_status_fk
        FOREIGN KEY (status) references notification_component_status (status),
    constraint message_template_summary_content_lookup_type_fk
        FOREIGN KEY (summary_content_lookup_type) references content_lookup_types (type),
    constraint message_template_body_content_lookup_type_fk
        FOREIGN KEY (body_content_lookup_type) references content_lookup_types (type)
);

--
-- Key value definition of criteria to be met by message context for a template to result in a delivered notification
create table delivery_criteria
(
    id                  BIGINT auto_increment,
    message_template_id BIGINT       not null,
    context_key         varchar(100) not null,
    value               varchar(100) not null,
    constraint delivery_criteria_pk
        primary key (id)
);

create table notification_deliveries
(
    id                  BIGINT auto_increment              not null,
    recipient           varchar(255)                       not null,
    message_template_id BIGINT                             not null,
    created_date        datetime DEFAULT CURRENT_TIMESTAMP not null,
    modified_date       datetime                           null,
    constraint notification_deliveries_pk
        primary key (id),
                                constraint notification_deliveries_message_template_fk
        FOREIGN KEY (message_template_id) references message_templates (id)
);

--
-- Those context keys used in the building of the context for a notification should be logged as metadata
create table notification_delivery_metadata
(
    id                       BIGINT auto_increment              not null,
    notification_delivery_id BIGINT                             not null,
    metadata_key             varchar(255)                       not null,
    metadata_value           varchar(255)                       not null,
    created_date             datetime DEFAULT CURRENT_TIMESTAMP not null,
    constraint notification_delivery_metadata_pk
        primary key (id),
    constraint notification_delivery_metadata_notification_delivery_fk
        FOREIGN KEY (notification_delivery_id) references notification_deliveries (id)
);
