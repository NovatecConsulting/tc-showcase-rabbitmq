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
A local RabbitMQ installation can be used to run the code.
Start a RabbitMQ Broker instance using Docker:

```
docker run --hostname my-rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3
```
### Tests and Setup
The project uses a [RabbitMQ testcontainer](https://www.testcontainers.org/modules/rabbitmq/) to execute tests without the need of a local RabbitMQ instance. 
After publishing your code to Github, build and test steps will be executed by a Github Actions pipeline.

Tests are written in Groovy using the [Spock Test Framework](https://spockframework.org/spock/docs/1.3/index.html).

Pull requests will automatically trigger a new test run.

## RabbitMQ Basics
### High-Level Model (AMQP 0-9-1)
RabbitMQ uses the following model:

Publishers send their messages to exchanges which can be compared to postoffices or mailboxes. These messages are then 
forwarded to one or more binded queues. Consumers can subscribe to queues or explicitly pull from them.

### 1. Exchanges
Exchanges route messages to one or more defined queues. There are four differnt types of exchanges:
- Direct exchange
- Fanout exchange
- Topic exchange
- Headers exchange

By default if no exchange is explicitly created, the broker will use the same name for the routing-key as for the queue
that the publisher wants to send the messages to.

### 2. Queues
RabbitMQ uses queues as a sequential data structure with the two primary operations of enqueueing and dequeueing. 
The important features are:
- Queues in RabbitMQ use FIFO prioritization.
- Applications can select a queue name or can request the broker to generate a name. The latter can be done by passing
an empty string where a queue name is expected.
- Queue names starting with "amq." are reserved for internal use only.

A queue can be declared with the properties name, durability, exclusivity, auto-deletion and additional arguments.

### 3. Connections and Channels
On the technical side, clients have to connect and authenticate to a RabbitMQ node before they are able to publish or consume
messages. 

Since there are some applications that need multiple connections, AMQP 0-9-1 offers to open one or more channels on a single
TCP connection. Like this, one TCP connection can be shared to save resources. The channels rely completely on the connection 
which means that if the connection is closed, all channels will be closed as well.

One example for the usage of multiple channels are applications that use multiple threads. For those applications, it is 
very common to open a new channel for each thread.

Exchanges and queues are completely independent of connections and channels. A client can publish to the same exchange
or consume from the same queue using multiple channels with one connection.

## Implementation of Messaging Patterns using RabbitMQ
### Competing Consumers
The Competing Consumers Pattern (also know as Point-to-Point) describes the scenario when one producer can publish
messages to a messaging queue. Those messages can be processed by any of multiple consumers while each message will only be processed once.

Further details: https://www.enterpriseintegrationpatterns.com/patterns/messaging/CompetingConsumers.html

The implementation of producer and consumer (including comments and further explanations) can be found at:
```
/src/main/java/competingconsumers/version091
```

