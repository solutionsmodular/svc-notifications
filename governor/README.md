# Notification Dispatcher
Written as a listener on the main bus, the Notification Dispatcher:

* subscribes to messages with known noun/verb combos
* filters messages to ones for which Message Configs exist
* records receipt of a message which matched existing Configs
* requests clearance from the Notification Message Governor to send messages
* records initiating send of message with Notification Delivery Log, preventing subsequent attempts (rapid receipt of a same message
* sends the Message Template details along with the message payload to the Message Dispatcher to merge, package, and deliver the notification message to the ultimate recipient

![](doc/dispatcher-sequence.png)

The following concerns are represented by this module.

## Notification Dispatcher

Dispatching a notification, at this level, is to determine what messages to send in response to a given SolBus message.

## Notification Config Cache

Yet undecided, but an alternative to hitting the DB.

## Notification Trigger Event Log

When an event triggers a notification, it must be logged as priority.
This is to minimize chances of rapid-fire triggering notifications.

## Notification Message Governor

The Message Governor centralizes concern and authorization for a particular message's delivery.
The Governor considers the following aspects when making the determination:

* recipient's preferences
* history of delivery of duplicate messages to recipient
* send rules as configured for the Message Template (send each event, send once, send x per y min)

## Notification Message Delivery Log

Not all triggers generate a notification. Some result in no op due to send rules.
The Delivery Log is a log of the actual handoff of a Message Template for delivery to a recipient.

## Recipient Notification Settings - See preferences

A user can configure the channels by which to receive notification messages.
These settings are made relative to modules managed by this project.

