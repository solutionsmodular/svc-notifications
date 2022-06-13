insert into local_static_content(tenant_id, content_key, content_block)
values (1, 'ORDER-PLACED-OWNER-EMAIL', 'Thank you for your order. %{msg.data.owner.firstName} '),
       (1, 'ORDER-RETURNED-OWNER-EMAIL',
        'Sorry it didn''t work out. Here''s a return label. ${msg.data.order.return.label}'),
       (1, 'CALEVENT-RSVPED-INVITEE-EMAIL',
        'Thank you for registering for %{calEvent.name}. %{calEvent.startDate} at %{calEvent.startTime}.'),
       (1, 'CALEVENT-STARTING',
        'You have registered for the %{calEvent.name} event, starting at %{calEvent.startDate} at %{calEvent.startTime}.'),
       (1, 'CALEVENT-ENDED',
        'Tell us what you think! How was your %{calEvent.name} service? Click here to take a short survey https://som-mod.io/surveys/%{calEvent.surveyCode}.');

