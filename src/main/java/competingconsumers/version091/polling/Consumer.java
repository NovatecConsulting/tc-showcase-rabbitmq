package competingconsumers.version091.polling;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Consumer {
    private static final Logger log = Logger.getLogger(Consumer.class.getName());
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private static final String TASK_QUEUE_NAME = "task_queue";
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;
    private java.util.function.Consumer<String> messageHandler;
    private AtomicBoolean running = new AtomicBoolean(true);

    /**
     * Establishes a new connection to a RabbitMQ Broker which runs locally. Declares a new channel and queue.
     *
     * @param port port number of the Broker to connect to
     */
    public Consumer(String host, int port, java.util.function.Consumer<String> messageHandler) throws IOException, TimeoutException {
        this.messageHandler = messageHandler;

        factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        connection = factory.newConnection();
        channel = connection.createChannel();

        channel.queueDeclare(TASK_QUEUE_NAME, false, false, false, null);
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

            while (running.get()) {
                //poll for messages -> blocking and less efficient, but might be useful/needed in some cases
                GetResponse response = channel.basicGet(TASK_QUEUE_NAME, false);
                if (response != null) {
                    String message = new String(response.getBody(), StandardCharsets.UTF_8);
                    System.out.println("Received " + message);

                    messageHandler.accept(message); //messageHandler will call method doWork()
                    System.out.println("Done.");

                    //send acknowledgement when finished
                    channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
                }
            }
        } catch (IOException e) {
            String warning = "Message could not be consumed and acknowledged.";
            log.log(Level.WARNING, warning, e);
        } finally {
            try {
                log.info("Stopping consumer...");
                connection.close();
                countDownLatch.countDown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Interrupts the while-loop for message consumption and therefore triggers connection closing.
     */
    public void stop() {
        running.set(false);
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }
}
