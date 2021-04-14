# RabbitMQ Showcase

Since RabbitMQ is increasingly being requested as an alternative to Kafka, Rabbit should be examined more closely for its advantages and disadvantages.
However, the focus of this repository should primarily be on getting to know RabbitMQ and AMQP (v0.9.1 and v1.0.0).

The overall goal should be to enable an Event Mesh with different protocols. This requires, among other things, the connection of RabbitMQ and the provision of AMQP endpoints.

### Objective
RabbitMQ is a versatile message broker that supports various messaging patterns. These are to be implemented using small sample implementations:

- Competing Consumers
- Publish-Subscribe Channel
- Consumer Group (consume messages in both, Publish-Subscribe Channel and Competing Consumers semantics)


### Prerequisites
A local RabbitMQ installation is needed to test and run the code.
Start a RabbitMQ Broker instance using Docker:

```
docker run --hostname my-rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3
```


## Implementation of Messaging Patterns using RabbitMQ
### Competing Consumers
The Competing Consumers Pattern (also know as Point-to-Point) describes the scenario when one producer can publish
messages to a messaging queue. Those messages can be processed by any of multiple consumers while each message will only be processed once.

Further details: https://www.enterpriseintegrationpatterns.com/patterns/messaging/CompetingConsumers.html

The implementation of producer and consumer (including comments and further explanations) can be found at:
```
/src/main/java/competingconsumers/version091
```

