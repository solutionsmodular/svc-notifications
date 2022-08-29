
insert into message_configs(notification_event_id, name, status)
select id, 'notification-admin-test-mc', 'A' from notification_events
where event_subject = 'ORDER' and event_verb = 'CREATED';

insert into message_templates(message_config_id, status, recipient_context_key, message_content_sender, content_key)
select mc.id, 'A', 'test-recipient-addy', 'EMAIL', 'ORDER_PLACED_OWNER_EMAIL' from notification_events ne
join message_configs mc on mc.notification_event_id = ne.id
where event_subject = 'ORDER' and event_verb = 'CREATED';


