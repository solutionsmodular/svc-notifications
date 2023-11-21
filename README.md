# Overview

The services and behavior supplied by this project include:

* Backing services used by Notification Admin
* Notification Engine, subscribing to the SolBus and triggering notifications per configuration as managed by Admin
* Failures queue, whereby an admin may present failures to customer service allowing a representative to manually compose the intended notification, or else fix the Content Manager and retry the notification. (https://solutionsmodular.atlassian.net/browse/NE-44)
* Senders

# Design Requirements

1. An admin must be able to specify multiple purposes (channels) for a content block.
For instance, a content block could be used for SMS _and_ timelines.
2. The engine must send all notifications configured.
In other words, if there is a notification configured for SMS (the concept of "send" in this context refers only to the choosing of messages to deliver. Rules and preferences are considered elsewhere)
3. How do I control what messages are sent via what sender?

# Settings Layers

The following describes the connection from an event on the bus to a specific piece of content being sent via a specific channel.

## Configuration

Configure the Notification Engine using settings for the following conceptual components:

### Notification Events

This defines message subject/verb under which different messages are ultimately delivered. For example, order created. 

### Message Templates

These specify a grouping of messages. Criteria for sending are here.

### Notification Messages

These specify the message template content and specify one or more senders.

Senders, such as email, timeline, or SMS, can not be duplicated within a Message Template 

## Sending

* An event is detected on the bus for which there is a Notification Event configured
* A Message Trigger is persisted to DB 
* Based on the message data, Message Templates whose data meets criteria are persisted as Message Deliveries
* Once the delivery is persisted, it can be queued for the Senders

TODO: rename messagetemplates to message_configurations