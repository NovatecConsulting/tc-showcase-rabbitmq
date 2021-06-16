## Competing Consumers Pattern: AMQP 1.0

In contrast to the RabbitMQ client, SwiftMQ implements a polling consumer, i.e., the consumer has to ask explicitly for messages.
To avoid blocking, it is possible to use the `receiveNoWait()` method that also provides the option to add a message listener.

