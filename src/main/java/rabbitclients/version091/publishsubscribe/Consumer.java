package rabbitclients.version091.publishsubscribe;

import com.rabbitmq.client.DeliverCallback;
import rabbitclients.AMQPConsumer;
import rabbitclients.EnvRabbitMQConfig;
import rabbitclients.RabbitMQConfig;
import rabbitclients.ReceiverApplication;
import rabbitclients.version091.BaseClient;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import static com.rabbitmq.client.BuiltinExchangeType.FANOUT;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Consumer extends BaseClient implements AMQPConsumer {
    private static final Logger log = Logger.getLogger(Consumer.class.getName());
    private String queueName;
    private String consumerTag;

    public Consumer(RabbitMQConfig rabbitMQConfig, java.util.function.Consumer<String> messageHandler)
            throws IOException, TimeoutException {
        super(rabbitMQConfig, messageHandler);
    }

    /**
     * Defines a callback-behaviour to process arriving messages which is executed as soon as a new message
     * is available on the specified queue.
     */
    @Override
    public void consumeMessages() {
        try {
            System.out.println(" ... Waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), UTF_8);
                System.out.println("Received '" + message + "'");

                getMessageHandler().accept(message);
                System.out.println("Done.");
                getChannel().basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };
            consumerTag = getChannel().basicConsume(queueName, false, deliverCallback, consumerTag -> {});
        } catch (IOException e) {
            log.warning("Message could not be consumed and acknowledged.");
        }
    }

    /**
     * Declares a new fanout exchange if it was not already created.
     * Declares a new exclusive queue and binds this queue to the exchange.
     * (Exclusive = queue that can only be accessed by the creating client and that
     * will be deleted if the client disconnects)
     * @throws IOException if exchange, queue or binding could not be declared
     */
    @Override
    public void prepareMessageExchange() throws IOException {
        getChannel().exchangeDeclare(getExchangeName(), FANOUT);
        queueName = getChannel().queueDeclare().getQueue();
        getChannel().queueBind(queueName, getExchangeName(), "");
    }

    /**
     * Cancel the message consumption and close the connection.
     * @throws IOException
     */
    @Override
    public void stop() throws IOException {
        log.info("Stopping client...");
        getChannel().basicCancel(consumerTag);
        getConnection().close();
    }

    /**
     * Start and test this consumer as a console application.
     * @param args
     * @throws IOException
     * @throws TimeoutException
     */
    public static void main(String[] args) throws IOException, TimeoutException {
        new ReceiverApplication(worker -> new Consumer(new EnvRabbitMQConfig(), worker)).start();
    }
}
