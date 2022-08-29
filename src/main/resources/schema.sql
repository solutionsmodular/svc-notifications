create table notification_component_statuses
(
    status      varchar(3) unique not null,
    description varchar(256)      not null,
    constraint status_pk
        primary key (status)
);

--
-- Describes the sender that a content block can be used via NotificationEngine. E.g. email, SMS
--
create table message_senders
(
    type        varchar(50) unique not null,
    description varchar(255)       not null,
    constraint message_senders_pk
        primary key (type)
);

--
create table notification_events
(
    id            BIGINT auto_increment,
    tenant_id     BIGINT                               not null,
    event_subject varchar(50)                          null,
    event_verb    varchar(50)                          null,
    status        varchar(3)                           not null,
    created_date  datetime DEFAULT CURRENT_TIMESTAMP   not null,
    modified_date datetime ON UPDATE CURRENT_TIMESTAMP null,
    constraint notification_events_pk
        primary key (id),
    constraint notification_context_status_fk
        FOREIGN KEY (status) references notification_component_statuses (status)
);

-- message_sender - links to message_senders describing what medium a given content supports
-- recipient_context_key - context value holding the value to be used as recipient (or prefix with two underscores,
-- indicating a constant)
--
create table message_configs
(
    id                    BIGINT auto_increment,
    notification_event_id BIGINT                               not null,
    name                  varchar(512)                         not null,
    status                varchar(3)                           not null,
    created_date          datetime DEFAULT CURRENT_TIMESTAMP   not null,
    modified_date         datetime ON UPDATE CURRENT_TIMESTAMP null,
    constraint message_configs_pk
        primary key (id),
    constraint message_configs_notification_event_fk
        FOREIGN KEY (notification_event_id) references notification_events (id),
    constraint message_configs_status_fk
        FOREIGN KEY (status) references notification_component_statuses (status)
);

--
-- message-template
--
create table message_templates
(
    id                    BIGINT auto_increment,
    message_config_id     BIGINT                               not null,
    recipient_context_key varchar(255)                         null,
    message_sender        varchar(50)                          NOT NULL,
    content_key           varchar(50)                          not null,
    status                varchar(3)                           not null,
    created_date          datetime DEFAULT CURRENT_TIMESTAMP   not null,
    modified_date         datetime ON UPDATE CURRENT_TIMESTAMP null,
    constraint message_templates_pk
        primary key (id),
    constraint message_templates_notification_context_fk
        FOREIGN KEY (message_config_id) references message_configs (id),
    constraint message_templates_status_fk
        FOREIGN KEY (status) references notification_component_statuses (status),
    constraint message_templates_summary_message_sender_fk
        FOREIGN KEY (message_sender) references message_senders (type)
);

--
-- Key value definition of criteria to be met by message context for a template to result in a delivered notification
create table delivery_criteria
(
    id                BIGINT auto_increment,
    message_config_id BIGINT       not null,
    context_key       varchar(100) not null,
    value             varchar(100) not null,
    constraint delivery_criteria_pk
        primary key (id),
    constraint delivery_criteria_message_config_fk
        FOREIGN KEY (message_config_id) references message_configs (id)
);

--
-- notification_triggers - Instances of a notification being triggered. All asynchronous calls will be able to key
-- off this baseline
create table notification_triggers
(
    id                    BIGINT auto_increment              not null,
    notification_event_id BIGINT                             not null,
    uid                   varchar(255)                       not null,
    status                varchar(3)                         not null,
    created_date          datetime DEFAULT CURRENT_TIMESTAMP not null,
    modified_date         datetime                           null,
    constraint notification_events_pk
        primary key (id),
    constraint notification_triggers_notification_event_fk
        FOREIGN KEY (notification_event_id) references notification_events (id),
    constraint notification_trigger_status_fk
        FOREIGN KEY (status) references notification_component_statuses (status)
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
    id                   BIGINT auto_increment              not null,
    recipient            varchar(255)                       not null, -- Still in design. Global ID of a user/contact
    message_template_id  BIGINT                             not null,
    message_body_uri     varchar(255)                       null,
    delivery_process_key varchar(255)                       null,
    status               varchar(3)                         not null,
    created_date         datetime DEFAULT CURRENT_TIMESTAMP not null,
    modified_date        datetime                           null,
    constraint notification_deliveries_pk
        primary key (id),
    constraint notification_deliveries_message_template_fk
        FOREIGN KEY (message_template_id) references message_templates (id)
);
