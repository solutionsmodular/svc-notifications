# Notification Engine - Overview

The services and behavior supplied by this project include:

* [admin](./admin/README.md) - Standard services for administration of the Notification Engine, used both by admin UI RESTfully, as well as modules herein
* [content-manager](./content-manager/README.md) - Soon to be a multi-tenanted SaaS used herein to manage the message content
* [dispatcher](./dispatcher/README.md) - Subscribes to the SolBus and triggers notifications per configuration as managed by Admin
* [governor](./governor/README.md) - Suppresses delivery of notifications based on rules and recipient preferences
* [preferences](./preferences/README.md) - Manages recipients' options for the messages and channels to allow

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

### Event Trigger

This defines message subject/verb under which different messages are ultimately delivered. For example, order created. 

### Message Trigger

Not to be mistaken with an Event Trigger, which is the event which triggers a notification, the Message Trigger is a record of 
a message which triggers the engine.

### Message Config

Defines criteria which is compared against an Event Message metadata.
The Message Config groups Message Templates.
Criteria allow for a type of message to be sent based on a message.
For example, an Order Placed message could send a "Thank you" message to the purchaser, and a "You've made a sale" message to the sponsor.

### Message Template

These are the individual message bodies sent, where each Message Template is the content for a different channel. 
For example, in response to an Order Placed message the email message could contain details where the SMS message would be more concise.

Senders, such as email, timeline, or SMS, can not be duplicated within a Message Template.

## Sending

* An event is detected on the bus for which there is a Notification Event configured
* A Message Trigger is persisted to DB 
* Based on the message data, Message Templates whose data meets criteria are persisted as Message Deliveries
* Once the delivery is persisted, it can be queued for the Senders

