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


insert into local_content(tenant_id, namespace, content_key, content_block)
values (1, 'notification_engine', 'ORDER_PLACED_OWNER_EMAIL',
        'Your order has been placed|||Thank you for your order. %{msg.data.owner.firstName}.'),
       (1, 'notification_engine', 'ORDER_PLACED_SPONSOR_EMAIL',
        ''),
       (1, 'notification_engine', 'ORDER_RETURNED_OWNER_EMAIL',
        'Your order return was processed|||Sorry it didn''t work out. Here''s a return label. ${msg.data.order.return.label}'),
       (1, 'notification_engine', 'CALEVENT_RSVPED_INVITEE_EMAIL',
        'You are registered to attend ${calEvent.name}|||Thank you for registering for %{calEvent.name}. %{calEvent.startDate} at %{calEvent.startTime}.'),
       (1, 'notification_engine', 'CALEVENT_STARTING_EMAIL',
        'Your event is about to begin|||You have registered for the %{calEvent.name} event, starting at %{calEvent.startDate} at %{calEvent.startTime}.'),
       (1, 'notification_engine', 'CALEVENT_ENDED_EMAIL',
        'Tell us what you think!|||How was your %{calEvent.name} service? Click here to take a short survey https://som_mod.io/surveys/%{calEvent.surveyCode}.');

insert into local_content_merge_fields(local_content_id, content_key, merge_field_name)
select id, 'msg.data.owner.firstName', 'First Name' from local_content
where content_key = 'ORDER_PLACED_OWNER_EMAIL'
UNION
select id, 'css.orderdetails', 'Order details CSS' from local_content
where content_key = 'ORDER_PLACED_SPONSOR_EMAIL'
UNION
select id, 'data.order.details', 'Order Details' from local_content
where content_key = 'ORDER_PLACED_SPONSOR_EMAIL'
UNION
select id, 'msg.data.order.return.label', 'Return Label' from local_content
where content_key = 'ORDER_RETURNED_OWNER_EMAIL'
UNION
select id, 'calEvent.startDate', 'Start Date' from local_content
where content_key = 'CALEVENT_RSVPED_INVITEE_EMAIL'
UNION
select id, 'calEvent.startTime', 'Start Time' from local_content
where content_key = 'CALEVENT_RSVPED_INVITEE_EMAIL'
UNION
select id, 'calEvent.name', 'Event Name' from local_content
where content_key = 'CALEVENT_RSVPED_INVITEE_EMAIL'
UNION
select id, 'calEvent.startDate', 'Start Date' from local_content
where content_key = 'CALEVENT_STARTING_EMAIL'
UNION
select id, 'calEvent.startTime', 'Start Time' from local_content
where content_key = 'CALEVENT_STARTING_EMAIL'
UNION
select id, 'calEvent.name', 'Event Name' from local_content
where content_key = 'CALEVENT_STARTING_EMAIL'
UNION
select id, 'calEvent.surveyCode', 'Survey Code' from local_content
where content_key = 'CALEVENT_ENDED_EMAIL'
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

