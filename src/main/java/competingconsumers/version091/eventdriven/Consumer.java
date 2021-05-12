package competingconsumers.version091.eventdriven;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class Consumer {
    private static final String TASK_QUEUE_NAME = "task_queue";
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;
    private java.util.function.Consumer<String> messageHandler;

    /**
     * Establishes a new connection to a RabbitMQ Broker which runs locally. Declares a new channel and queue.
     * @param port port number of the Broker to connect to
     */
    public Consumer(int port, java.util.function.Consumer<String> messageHandler) {
        try {
            this.messageHandler = messageHandler;

            factory = new ConnectionFactory();
            factory.setHost("localhost");
            factory.setPort(port);
            connection = factory.newConnection();
            channel = connection.createChannel();

            channel.queueDeclare(TASK_QUEUE_NAME, false, false, false, null);
        }catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    /**
     * Implements the process of message consumption: waits for messages, triggers processing
     * and sends an acknowledgement.
     */
    public void consumeMessages() {
        try {
            System.out.println(" ... Waiting for messages. To exit press CTRL+C");

            //enable fair dispatch: if this consumer is still busy, RabbitMQ will assign the task to the next consumer.
            //Important: if all consumers are busy, the queue will fill up!
            channel.basicQos(1);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

                System.out.println("Received '" + message + "'");
                try {
                    messageHandler.accept(message);
                } finally {
                    System.out.println("Done.");
                    //send acknowledgement when finished
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };
            //no auto-acknowledgement!
            channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> {
            });
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            System.out.println("Stopping consumer...");
            connection.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
