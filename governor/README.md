# Overview - Notification Message Governor

The Message Governor centralizes concern and authorization for a particular message's delivery.
The Governor considers the following aspects when making the determination:

* recipient's preferences
* history of delivery of duplicate messages to recipient
* send rules as configured for the Message Template (send each event, send once, send x per y min)

