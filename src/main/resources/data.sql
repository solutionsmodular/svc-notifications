insert into notification_component_statuses (status, description)
values ('A', 'Active'),
       ('I', 'Inactive'),
       ('D', 'Deleted'),
       ('V', 'Delivered'),
       ('F', 'Fail'),
       ('TO', 'Timed Out'),
       ('PC', 'Pending Context'),
       ('PP', 'Pending Permission'),
       ('PD', 'Pending Delivery')
       ;
insert into message_senders(type, description)
values ('EMAIL', 'Suited to Email'),
       ('SMS', 'Suited to SMS'),
       ('PUSH', 'Suited to Push Notifications'),
       ('TIMELINE', 'Suited to Timeline entry')
       ;

insert into notification_events(tenant_id, event_subject, event_verb, status)
values (1, 'ORDER', 'CREATED', 'A'),
       (1, 'ORDER', 'RETURNED', 'A'),
       (1, 'CAL_EVENT', 'RSVPED', 'A'),
       (1, 'CAL_EVENT', 'STARTING', 'A'),
       (1, 'CAL_EVENT', 'ENDING', 'A')
;

-- Set up some Configs
insert into message_configs(notification_event_id, name, status)
select id, 'Order Created', 'A' from notification_events
where event_subject = 'ORDER' and event_verb = 'CREATED'
UNION
select id, 'Order Returned','A' from notification_events
where event_subject = 'ORDER' and event_verb = 'RETURNED'
UNION
select id, 'Calendar Event Received RSVP', 'A' from notification_events
where event_subject = 'CAL_EVENT' and event_verb = 'RSVPED'
UNION
select id, 'Calendar Event About to Start', 'A' from notification_events
where event_subject = 'CAL_EVENT' and event_verb = 'STARTING'
UNION
select id, 'Calendar Event Ending Soon', 'A' from notification_events
where event_subject = 'CAL_EVENT' and event_verb = 'ENDING'
;

insert into message_templates(message_config_id, status, recipient_context_key, message_sender, content_key)
select mc.id, 'A', 'data.order.owner.email', 'EMAIL', 'ORDER_PLACED_OWNER_EMAIL' from notification_events ne
join message_configs mc on mc.notification_event_id = ne.id
where event_subject = 'ORDER' and event_verb = 'CREATED'
UNION
select mc.id, 'A', 'data.order.owner.sponsor.email', 'EMAIL', 'ORDER_PLACED_SPONSOR_EMAIL' from notification_events ne
join message_configs mc on mc.notification_event_id = ne.id
where event_subject = 'ORDER' and event_verb = 'CREATED'
UNION
select mc.id, 'A', 'data.order.owner.email', 'EMAIL', 'ORDER_RETURNED_OWNER_EMAIL' from notification_events ne
join message_configs mc on mc.notification_event_id = ne.id
where event_subject = 'ORDER' and event_verb = 'RETURNED'
UNION
select mc.id, 'A', 'data.order.owner.email', 'EMAIL', 'CALEVENT_RSVPED_INVITEE_EMAIL' from notification_events ne
join message_configs mc on mc.notification_event_id = ne.id
where event_subject = 'CAL_EVENT' and event_verb = 'RSVPED'
UNION
select mc.id, 'A', 'data.order.owner.email', 'EMAIL', 'CALEVENT_STARTING_EMAIL' from notification_events ne
join message_configs mc on mc.notification_event_id = ne.id
where event_subject = 'CAL_EVENT' and event_verb = 'STARTING'
UNION
select mc.id, 'A', 'data.order.owner.email', 'EMAIL', 'CALEVENT_ENDING_EMAIL' from notification_events ne
join message_configs mc on mc.notification_event_id = ne.id
where event_subject = 'CAL_EVENT' and event_verb = 'ENDING'
;

insert into notification_triggers(notification_event_id, uid, status)
select mc.id, 'existing-uid', 'A' from notification_events ne
join message_configs mc on mc.notification_event_id = ne.id
where event_subject = 'ORDER' and event_verb = 'CREATED'
;

