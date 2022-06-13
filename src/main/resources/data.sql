insert into notification_component_status (status, description)
values ('A', 'Active'),
       ('I', 'Inactive'),
       ('D', 'Deleted');
insert into content_lookup_types(type, description)
values ('URL', 'Static content HTML. Key should be http or https URL'),
       ('CONTENT_KEY', 'Using SolMod infrastructure, this content key should be used for content management'),
       ('STATIC', 'Reference the local_static_content table by content key');
insert into message_templates(tenant_id, event_subject, event_verb, status, recipient_context_key,
                                 summary_content_lookup_type,
                                 summary_content_key, body_content_lookup_type, body_content_key)
values (1, 'something', 'happened', 'A', 'msg.contextRequest.recipientAddress', 'STATIC', 'something-happened-summary',
        'STATIC', 'something-happened-body');
