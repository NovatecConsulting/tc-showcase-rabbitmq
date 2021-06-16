## AMQP 1.0
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

The number of available Java clients that support AMQP 1-0 is still limited at the moment:

- [SwiftMQ](https://www.swiftmq.com/docs/docs/client/amqp/): 
The plugin's developers tested the functionalities mainly using SwiftMQ.
- [Qpid Proton-J](https://qpid.apache.org/releases/qpid-proton-j-0.33.8/):
The documentation is not that detailed and does not provide examples.
- [Qpid JMS](https://qpid.apache.org/components/jms/index.html):
Uses Proton-J internally. According to the documentation, the plugin's developers could not establish a connection using Qpid JMS/Proton-J (!)

Since Qpid was not (successfully) tested with the AMQP-plugin, this project uses the SwiftMQ client.

### AMQP 1-0 Protocol
Important AMQP 1-0 message fields MUST not be set by the application because they are overwritten by the Producer:
- Header fields: durable, priority, ttl
- Property fields: messageId, to, userId

Additional fields that are provided by AMQP 1-0:
- Header fields: first-acquirer, delivery-count
- Property fields: subject, reply-to, correlation-id, content-type, content-encoding, absolute-expiry-time, creation-time

A short comparison of the AMQP 0-9-1 and AMQP 1-0 fields is also provided 
in the [RabbitMQ plugin documentation](https://github.com/rabbitmq/rabbitmq-amqp1.0#message-properties-annotations-headers-etc).

### Routing and Addresses
If no specific exchange or topic name is provided, the messages are sent to the default exchange with the queue name
as routing key.
To specify exchange, topic or queue names, the address schema like described in the
[plugin documentation](https://github.com/rabbitmq/rabbitmq-amqp1.0#routing-and-addressing) can be used.

AMQP 1.0 was developed to be universally applicable and therefore does not know concepts like topics or queues.
When creating a producer or consumer, addresses can be used to set the target/source of messages:
```
com.swiftmq.amqp.v100.client.Producer producerInstance = session.createProducer(/topic/my_routing_key, qos);
```
In the example above, the producer sends all messages to the *amq.topic exchange*, from where
they are then further distributed to all queues with the binding *my_routing_key*.

### Frame Sizes and Session Windows
For each connection, a maximum frame size has to be set to make sure that the sender/receiver has enough capacity 
to process the message. Messages that are too large for one frame will be split internally by SwiftMQ into multiple frames
forming one so-called "Delivery".

The frames are buffered in an outgoing/incoming session window with a limited size. Before they can be consumed as an
AMQPMessage, the frames are put together again internally. The protocol implementation and the RabbitMQ plugin handle
the frame sequence and lost frames according to the quality of service. Therefore, this does not need to be handled
by the Java Client.

### Settlement and Quality of Service
There are three different qualities of service available for clients:
1. **At-Most-Once / Fire-and-Forget**    
Sent messages are already in the state "settled". The sender won't wait until the receiver has successfully
received the message and will immediately forget about it. If the message is already settled, the receiver will never send an acknowledgement because
even if it would do so, the sender would already have forgotten about the message.

2. **At-Least-Once**  
The sender waits for a reply before it marks the message as settled.
Released and modified messages are sent again. In case the receiver or sender fails,
the link can be re-established: The nodes compare their unsettled messages, update their states accordingly, 
eventually re-sent the frames and clean their buffers.

3. **Exactly-Once**  
According to AMQP 1-0, sender and receiver must be able to renegotiate the delivery status in order to 
ensure "Exactly-Once"-quality. RabbitMQ does not support this concept.

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
