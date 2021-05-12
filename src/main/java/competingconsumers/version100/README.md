## Competing Consumers Pattern - AMQP 1.0
### Plugin Installation
RabbitMQ primarily supports AMQP 0-9-1 and uses a plugin mechanism to enable the use of AMQP 1-0. 
To run the code, a RabbitMQ broker with the enabled plugin has to be started using the Dockerfile located in this directory:
```
docker build -f Dockerfile -t rabbitmq-amqp-1_0-enabled .
docker run --hostname my-rabbit -p 5672:5672 -p 15672:15672 rabbitmq-amqp-1_0-enabled
```
Alternatively, you can pull the image from Dockerhub:
```
docker run --hostname my-rabbit -p 5672:5672 -p 15672:15672 nadjahagen/rabbitmq-amqp-1_0-enabled:latest
```
Important: Without exposing the ports explicitly, the application won't be able to connect to the broker.
A ConnectionRefusedException will occur.

### Client Library
As already mentioned, RabbitMQ does not natively support AMQP 1-0. Therefore,
[their Java Client does not support AMQP 1-0](https://github.com/rabbitmq/rabbitmq-java-client/issues/651) 
which is also not planned for the future.

The number of available Java clients, that support AMQP 1-0, is still limited at the moment:

- [SwiftMQ](https://www.swiftmq.com/docs/docs/client/amqp/): 
The plugin's developers tested the functionalities mainly using SwiftMQ.
- [Qpid Proton-J](https://qpid.apache.org/releases/qpid-proton-j-0.33.8/):
The documentation is not that detailed and does not provide examples.
- [Qpid JMS](https://qpid.apache.org/components/jms/index.html):
Uses Proton-J under the hood. According to the documentation, the plugin's developers were not able to establish a connection using Qpid JMS/Proton-J (!)

Since Qpid was not (successfully) tested with the AMQP-plugin, this project uses the SwiftMQ client.

### Pattern Implementation
In contrast to the RabbitMQ client, SwiftMQ implements a polling consumer, i.e. the consumer has to ask explicitly for messages.
To avoid blocking, it is possible to use the `receiveNoWait()` method which also provides the option to add a message listener.

### AMQP 1-0 Protocol
Important AMQP 1-0 message fields MUST not be set by the application because they are overwritten by the Producer:
- Header fields: durable, priority, ttl
- Property fields: messageId, to, userId

Additional fields that are provided by AMQP 1-0:
- Header fields: first-acquirer, delivery-count
- Property fields: subject, reply-to, correlation-id, content-type, content-encoding, absolute-expiry-time, creation-time

A short comparison of the AMQP 0-9-1 and AMQP 1-0 fields is also provided 
in the [RabbitMQ plugin documentation](https://github.com/rabbitmq/rabbitmq-amqp1.0#message-properties-annotations-headers-etc).

### Error Handling
A ConnectionClosedException will occur if the plugin is not enabled for the RabbitMQ broker. 
The connection will be refused because of conflicting AMQP protocol versions:
```
com.swiftmq.amqp.v100.client.ConnectionClosedException: java.io.IOException: End-of-Stream reached 

    at com.swiftmq.amqp.v100.client.Connection$2.run(Connection.java:432) 

    com.swiftmq.amqp.v100.client.UnsupportedProtocolVersionException: Incompatible AMQP protocols. Local=[ProtocolHeader, name=AMQP, id=0, major=1, minor=0, revision=0], remote=[ProtocolHeader, name=AMQP, id=0, major=0, minor=9, revision=1] 
    ...
```
To resolve this, enable the plugin like described above at *Plugin Installation*.
