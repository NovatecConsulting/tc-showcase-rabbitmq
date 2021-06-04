## Publish-Subscribe Pattern: AMQP 1.0

AMQP 1.0 does not know concepts like queues or topics. The target (or source) of a message has to be specified using 
the address-field of an AMQP 1.0 message like it is described in the [plugin's documentation](https://github.com/rabbitmq/rabbitmq-amqp1.0#routing-and-addressing).

### Issues and Constraints 
Since this concept does not exist in AMQP 1.0, the SwiftMQ Java Client does not offer the possibility to manage 
queues, topics and bindings like it is possible with the RabbitMQ Java Client for AMQP 0-9-1. Therefore, exchanges
and queues need to already exist when the application is started or can be created using RabbitMQ's REST API.

The publish-subscribe pattern requires one queue for each subscriber. The publisher sends all messages to a fanout
exchange from where they will be broadcasted to all queues with a corresponding binding. Since the queue name is
different for each subscriber, the name has to be passed into the program (if the queue already exists) or 
the queue has to be created within the application via the RabbitMQ REST API.
Both possibilities come along with advantages and disadvantages which is why SwiftMQ and AMQP 1.0 are probably not 
suitable for the publish-subscribe pattern.

In this repository, the Setup-class implements methods for the creation of exchanges, queues and bindings via REST. 
The entities that are created with HTTP methods are not client-bound and therefore cannot be created as "exclusive".
This has the disadvantage that the queues are not automatically deleted when the client disconnects from the broker.
In addition, the REST API does not offer to create queues without names to let the broker choose a (temporary) unique
queue name. This leads to the constraint, that unique names have to be managed by the application.
