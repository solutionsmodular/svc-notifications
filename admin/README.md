# Admin - Overview

This module is responsible for managing the configurations for the Notification Engine.

# Configuration Layers

Configure the Notification Engine using settings for the following conceptual components:

## Notification Group

This defines message subject/verb under which different messages are ultimately delivered. For example, order created.

## Message Theme

The Message Theme groups Message Forms.
Defines criteria which is compared against a SolBus Message metadata.
Criteria allow for a type of message to be sent based on a message.
For example, an Order Placed message could send a "Thank you" message to the purchaser, and a "You've made a sale" message to the sponsor.

## Message Template

These define the content and construct of the ultimate message to send.
They allow for dynamic data to be merged using the metadata key name surrounded with {{}}.
There may be several Message Forms for any given Message Theme, where each Message Template is the content appropriate for the different channels.
For example, in response to an Order Placed message the email message could contain details where the SMS message would be more concise.

Only one Message Form can be defined within the scope of a Message Theme for any given senders (e.g. email, SMS, timeline)

