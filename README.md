# Notification Engine - Overview

The services and behavior supplied by this project include:

* [admin](./admin/README.md) - Standard services for administration and access of data for the Notification Engine
* [content-manager](./content-manager/README.md) - Used to manage the message content
* [dispatcher](./dispatcher/README.md) - Subscribes to the SolBus and triggers notifications per configuration as managed by Admin
* [governor](./governor/README.md) - Suppresses delivery of notifications based on rules and recipient preferences
* [preferences](./preferences/README.md) - Manages recipients' options for the messages and channels to allow

I

# Design Requirements

1. An admin must be able to specify multiple purposes (channels) for a Message Template (ultimately resolving to a content block).
For instance, a content block could be used for SMS _and_ timelines.
2. The engine must send all notifications configured which apply to a given SolBus Message.
In other words, if there is a notification configured for SMS and Timeline, then those will be delivered (this is an 
3. over-simplification; actually delivering a message requires clearance from the Governor)
4. How do I control what messages are sent via what sender?

# Terms

Not rocket science, but there are some ambiguities among the components of the Notification Engine.

## Message

The concept of a "Message" could mean the event which is sent over the SolBus (e.g. JSON), for which a notification might need to be delivered. 
Or, it could mean the actual message as can ultimately be viewed on a mobile device via SMS.
Therefore, the term "Message" is not used within this documentation without full qualification (e.g. Message Template vs 
SolBus Message vs Rendered Message).
I've decided it's an intuitive substitution to refer to a "Content Block" (to which a Message Template ultimately resolves) 
to refer to a Message Template, or an "Event Message" to refer to a SolBus message.
