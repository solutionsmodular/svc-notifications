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
create table message_content_purposes
(
    type        varchar(50) unique not null,
    description varchar(255)       not null,
    constraint message_content_purposes_pk
        primary key (type)
);

-- In systems not using Content Manager, Notification Engine can store its own content
create table local_content
(
    id            BIGINT auto_increment,
    tenant_id     BIGINT,
    namespace     varchar(255)                         not null,
    content_key   varchar(255)                         not null,
    content_block blob                                 not null,
    created_date  datetime DEFAULT CURRENT_TIMESTAMP   not null,
    modified_date datetime ON UPDATE CURRENT_TIMESTAMP null,
    constraint local_content_pk
        primary key (id)
);

-- When content is saved, merge fields are parsed out and cataloged for ease of reference
create table local_content_merge_fields
(
    local_content_id BIGINT,
    content_key      varchar(50)  not null,
    merge_field_name varchar(255) not null,
    constraint content_merge_fields_local_content_id_fk
        FOREIGN KEY (local_content_id) references local_content (id)
);

--
create table notification_events
(
    id            BIGINT auto_increment,
    tenant_id     BIGINT                               not null,
    event_subject varchar(50)                          null,
    event_verb    varchar(50)                          null,
    status        varchar(1)                           not null,
    created_date  datetime DEFAULT CURRENT_TIMESTAMP   not null,
    modified_date datetime ON UPDATE CURRENT_TIMESTAMP null,
    constraint notification_events_pk
        primary key (id),
    constraint notification_context_status_fk
        FOREIGN KEY (status) references notification_component_status (status)
);

--
-- Content type URL: key = URL
-- Content type CONTEXT_KEY: Use CMS
-- Content type LOCAL: key refers to local_content
-- recipient_context_key - context value holding the value to be used as recipient (or prefix with two underscores,
-- indicating a constant)
create table message_templates
(
    id                      BIGINT auto_increment,
    notification_event_id   BIGINT                               not null,
    recipient_context_key   varchar(255)                         null,
    message_content_purpose varchar(50)                          NOT NULL,
    content_key             varchar(50)                          not null,
    status                  varchar(1)                           not null,
    created_date            datetime DEFAULT CURRENT_TIMESTAMP   not null,
    modified_date           datetime ON UPDATE CURRENT_TIMESTAMP null,
    constraint message_templates_pk
        primary key (id),
    constraint message_templates_notification_context_fk
        FOREIGN KEY (notification_event_id) references notification_events (id),
    constraint message_templates_status_fk
        FOREIGN KEY (status) references notification_component_status (status),
    constraint message_template_summary_message_content_purpose_fk
        FOREIGN KEY (message_content_purpose) references message_content_purposes (type)
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

--
-- notification_triggers - Instances of a notification being triggered. All asynchronous calls will be able to key
-- off this baseline
create table notification_triggers
(
    id                    BIGINT auto_increment              not null,
    notification_event_id BIGINT                             not null,
    uid                   varchar(255)                       not null,
    status                varchar(1)                         not null,
    created_date          datetime DEFAULT CURRENT_TIMESTAMP not null,
    modified_date         datetime                           null,
    constraint notification_events_pk
        primary key (id),
    constraint notification_triggers_notification_event_fk
        FOREIGN KEY (notification_event_id) references notification_events (id),
    constraint notification_trigger_status_fk
        FOREIGN KEY (status) references notification_component_status (status)
);

--
-- The context values needed to initiate the context builder calls
create table notification_trigger_context
(
    id                      BIGINT auto_increment              not null,
    notification_trigger_id BIGINT                             not null,
    context_key             varchar(255)                       not null,
    context_value           varchar(255)                       not null,
    created_date            datetime DEFAULT CURRENT_TIMESTAMP not null,
    constraint notification_trigger_metadata_pk
        primary key (id),
    constraint notification_trigger_metadata_notification_event_fk
        FOREIGN KEY (notification_trigger_id) references notification_triggers (id)
);

create table notification_deliveries
(
    id                  BIGINT auto_increment              not null,
    recipient           varchar(255)                       not null,
    message_template_id BIGINT                             not null,
    status              varchar(1)                         not null,
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
