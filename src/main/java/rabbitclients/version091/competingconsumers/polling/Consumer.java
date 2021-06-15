package rabbitclients.version091.competingconsumers.polling;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import rabbitclients.AMQPConsumer;
import rabbitclients.RabbitMQConfig;
import rabbitclients.version091.BaseClient;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Consumer extends BaseClient implements AMQPConsumer, Runnable {
    private static final Logger log = Logger.getLogger(Consumer.class.getName());
    private AtomicBoolean running = new AtomicBoolean(true);
    private Thread getMessage;

    public Consumer(RabbitMQConfig rabbitMQConfig, java.util.function.Consumer<String> messageHandler)
            throws IOException, TimeoutException {
        super(rabbitMQConfig, messageHandler);
        prepareMessageExchange();
    }

    /**
     * Actively polls for new messages in a while-loop using the Channel.basicGet()-method.
     * basicGet() will immediately return if no new message is available and it is not possible
     * to specify an optional timeout duration.
     */
    @Override
    public void consumeMessages() {
        System.out.println(" ... Waiting for messages. To exit press CTRL+C");

        getMessage = new Thread(this); //get messages in polling behaviour in new thread
        getMessage.start();
    }

    /**
     * Declare a new queue on this session/channel if the queue was not already created.
     *
     * @throws IOException if queue could not be declared
     */
    @Override
    public void prepareMessageExchange() throws IOException {
        getChannel().queueDeclare(getQueueName(), false, false, false, null);
    }

    /**
     * Receive and acknowledge new messages from the broker using a while-loop and basicGet()-method.
     */
    @Override
    public void run() {
        try {
            Channel secondChannel = getConnection().createChannel();
            while (running.get()) {
                //new Channel because Channels must not be shared between threads
                //will immediately return if no new message is available, no timeout possible
                GetResponse response = secondChannel.basicGet(getQueueName(), false);
                if (response != null) {
                    String message = new String(response.getBody(), UTF_8);
                    System.out.println("Received " + message);

                    getMessageHandler().accept(message);
                    System.out.println("Done.");
                    secondChannel.basicAck(response.getEnvelope().getDeliveryTag(), false);
                }
            }
        } catch (IOException e) {
            log.severe("Message could not be consumed and acknowledged.");
        } finally {
            try {
                getConnection().close();
            } catch (IOException e) {
                log.warning("Connection could not be closed.");
            }
        }
    }

    /**
     * Stop the message consumption while-loop and join the thread.
     *
     * @throws InterruptedException
     */
    @Override
    public void stop() throws InterruptedException {
        running.set(false);
        getMessage.join();
    }
}
