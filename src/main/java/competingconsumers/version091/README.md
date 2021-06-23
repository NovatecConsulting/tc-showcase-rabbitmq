## Competing Consumers Pattern - AMQP 0.9.1
AMQP 0-9-1 is RabbitMQ's default protocol. There are various client libraries available that were developed by the 
RabbitMQ team to support AMQP 0-9-1.

### Eventdriven Consumer
Using an eventdriven consumer for the competing consumer pattern is considered as standard for RabbitMQ.
The basicConsume()-method uses the DeliverCallBack interface to notify the consumer as soon as a new message is available.
The consumer is not blocked while it is waiting for messages. 

```
DeliverCallback deliverCallback = (consumerTag, delivery) -> {
   String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

   System.out.println("Received '" + message + "'");
   try {
       messageHandler.accept(message);
   } finally {
       System.out.println("Done.");
       channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
   }
;

channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> { });
```
Read more about this type of consumer [here](https://www.enterpriseintegrationpatterns.com/patterns/messaging/EventDrivenConsumer.html).

### Polling Consumer
The RabbitMQ client also provides the possibility to use a polling consumer which needs to actively ask for new messages.
The *basicGet()*-method directly returns when no message is available and does not offer to specify a polling timeout.
Because of this implementation, the consumer has to constantly poll for messages using a loop 
which is highly inefficient and therefore not recommended.

```
GetResponse response = channel.basicGet(TASK_QUEUE_NAME, false);
```
Read more about this type of consumer [here](https://www.enterpriseintegrationpatterns.com/patterns/messaging/PollingConsumer.html).