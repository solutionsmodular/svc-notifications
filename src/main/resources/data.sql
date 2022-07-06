insert into notification_component_status (status, description)
values ('A', 'Active'),
       ('I', 'Inactive'),
       ('D', 'Deleted'),
       ('V', 'Delivered'),
       ('V', 'Canceled'),
       ('F', 'Fail'),
       ('PP', 'Pending Permission'),
       ('PC', 'Pending Context'),
       ('PD', 'Pending Delivery')
       ;
insert into content_lookup_types(type, description)
values ('URL', 'Static content HTML. Key should be http or https URL'),
       ('CONTENT_KEY', 'Using SolMod infrastructure, this content key should be used for content management'),
       ('STATIC', 'Reference the local_static_content table by content key');


insert into local_static_content(tenant_id, namespace, content_key, content_block)
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

insert into content_merge_fields(local_static_content_id, content_key, merge_field_name)
select id, 'msg.data.owner.firstName', 'First Name' from local_static_content
where content_key = 'ORDER_PLACED_OWNER_EMAIL'
UNION
select id, 'css.orderdetails', 'Order details CSS' from local_static_content
where content_key = 'ORDER_PLACED_SPONSOR_EMAIL'
UNION
select id, 'data.order.details', 'Order Details' from local_static_content
where content_key = 'ORDER_PLACED_SPONSOR_EMAIL'
UNION
select id, 'msg.data.order.return.label', 'Return Label' from local_static_content
where content_key = 'ORDER_RETURNED_OWNER_EMAIL'
UNION
select id, 'calEvent.startDate', 'Start Date' from local_static_content
where content_key = 'CALEVENT_RSVPED_INVITEE_EMAIL'
UNION
select id, 'calEvent.startTime', 'Start Time' from local_static_content
where content_key = 'CALEVENT_RSVPED_INVITEE_EMAIL'
UNION
select id, 'calEvent.name', 'Event Name' from local_static_content
where content_key = 'CALEVENT_RSVPED_INVITEE_EMAIL'
UNION
select id, 'calEvent.startDate', 'Start Date' from local_static_content
where content_key = 'CALEVENT_STARTING_EMAIL'
UNION
select id, 'calEvent.startTime', 'Start Time' from local_static_content
where content_key = 'CALEVENT_STARTING_EMAIL'
UNION
select id, 'calEvent.name', 'Event Name' from local_static_content
where content_key = 'CALEVENT_STARTING_EMAIL'
UNION
select id, 'calEvent.surveyCode', 'Survey Code' from local_static_content
where content_key = 'CALEVENT_ENDED_EMAIL'
;

insert into notification_contexts(tenant_id, event_subject, event_verb, status)
values (1, 'ORDER', 'CREATED', 'A'),
       (1, 'ORDER', 'RETURNED', 'A'),
       (1, 'CAL_EVENT', 'RSVPED', 'A'),
       (1, 'CAL_EVENT', 'STARTING', 'A'),
       (1, 'CAL_EVENT', 'ENDING', 'A');

insert into message_templates(notification_context_id, status, recipient_context_key, content_lookup_type, content_key)
select id, 'A', 'data.order.owner.email', 'STATIC', 'ORDER_PLACED_OWNER_EMAIL' from notification_contexts
where event_subject = 'ORDER' and event_verb = 'CREATED'
UNION
select id, 'A', 'data.order.owner.sponsor.email', 'STATIC', 'ORDER_PLACED_SPONSOR_EMAIL' from notification_contexts
where event_subject = 'ORDER' and event_verb = 'CREATED'
UNION
select id, 'A', 'data.order.owner.email', 'STATIC', 'ORDER_RETURNED_OWNER_EMAIL' from notification_contexts
where event_subject = 'ORDER' and event_verb = 'RETURNED'
UNION
select id, 'A', 'data.order.owner.email', 'STATIC', 'CALEVENT_RSVPED_INVITEE_EMAIL' from notification_contexts
where event_subject = 'CAL_EVENT' and event_verb = 'RSVPED'
UNION
select id, 'A', 'data.order.owner.email', 'STATIC', 'CALEVENT_STARTING_EMAIL' from notification_contexts
where event_subject = 'CAL_EVENT' and event_verb = 'STARTING'
UNION
select id, 'A', 'data.order.owner.email', 'STATIC', 'CALEVENT_ENDING_EMAIL' from notification_contexts
where event_subject = 'CAL_EVENT' and event_verb = 'ENDING'
;
