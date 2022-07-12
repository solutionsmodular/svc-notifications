insert into notification_component_status (status, description)
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
insert into message_content_purposes(type, description)
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

insert into message_templates(notification_event_id, status, recipient_context_key, message_content_purpose, content_key)
select id, 'A', 'data.order.owner.email', 'EMAIL', 'ORDER_PLACED_OWNER_EMAIL' from notification_events
where event_subject = 'ORDER' and event_verb = 'CREATED'
UNION
select id, 'A', 'data.order.owner.sponsor.email', 'EMAIL', 'ORDER_PLACED_SPONSOR_EMAIL' from notification_events
where event_subject = 'ORDER' and event_verb = 'CREATED'
UNION
select id, 'A', 'data.order.owner.email', 'EMAIL', 'ORDER_RETURNED_OWNER_EMAIL' from notification_events
where event_subject = 'ORDER' and event_verb = 'RETURNED'
UNION
select id, 'A', 'data.order.owner.email', 'EMAIL', 'CALEVENT_RSVPED_INVITEE_EMAIL' from notification_events
where event_subject = 'CAL_EVENT' and event_verb = 'RSVPED'
UNION
select id, 'A', 'data.order.owner.email', 'EMAIL', 'CALEVENT_STARTING_EMAIL' from notification_events
where event_subject = 'CAL_EVENT' and event_verb = 'STARTING'
UNION
select id, 'A', 'data.order.owner.email', 'EMAIL', 'CALEVENT_ENDING_EMAIL' from notification_events
where event_subject = 'CAL_EVENT' and event_verb = 'ENDING'
;

insert into notification_triggers(notification_event_id, uid, status)
select id, 'existing-uid', 'A' from notification_events where event_subject = 'ORDER' and event_verb = 'CREATED'
;

