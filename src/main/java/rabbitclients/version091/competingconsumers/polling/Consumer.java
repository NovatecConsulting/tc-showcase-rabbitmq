package rabbitclients.version091.competingconsumers.polling;

import com.rabbitmq.client.GetResponse;
import rabbitclients.AMQPClient;
import rabbitclients.version091.BaseClient;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Consumer extends BaseClient implements AMQPClient {
    private static final Logger log = Logger.getLogger(Consumer.class.getName());
    private AtomicBoolean running = new AtomicBoolean(true);

    public Consumer(String host, int port, java.util.function.Consumer<String> messageHandler)
            throws IOException, TimeoutException {
        super(host, port, messageHandler);
        prepareMessageExchange();
    }

    /**
     * Actively polls for new messages in a while-loop using the Channel.basicGet()-method.
     * basicGet() will immediately return if no new message is available and it is not possible
     * to specify an optional timeout duration.
     */
    @Override
    public void consumeMessages() {
        try {
            System.out.println(" ... Waiting for messages. To exit press CTRL+C");
            getChannel().basicQos(1);

            while (running.get()) {
                //will immediately return if no new message is available, no timeout possible
                GetResponse response = getChannel().basicGet(getQueueName(), false);
                if (response != null) {
                    String message = new String(response.getBody(), UTF_8);
                    System.out.println("Received " + message);

                    getMessageHandler().accept(message);
                    System.out.println("Done.");
                    getChannel().basicAck(response.getEnvelope().getDeliveryTag(), false);
                }
            }
        } catch (IOException e) {
            log.warning("Message could not be consumed and acknowledged.");
        } finally {
            try {
                log.info("Stopping client...");
                getChannel().getConnection().close();
                getCountDownLatch().countDown();
            } catch (IOException e) {
                log.severe("Could not close connection.");
            }
        }
    }

    /**
     * Declare a new queue on this session/channel if the queue was not already created.
     * @throws IOException if queue could not be declared
     */
    public void prepareMessageExchange() throws IOException {
        getChannel().queueDeclare(getQueueName(), false, false, false, null);
    }

    /**
     * Interrupts the while-loop in the consumeMessages()-method to gracefully terminate the application.
     */
    @Override
    public void stop() {
        running.set(false);
    }
}
