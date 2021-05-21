package competingconsumers.version091;

import com.rabbitmq.client.*;
import competingconsumers.version091.polling.Consumer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Producer {
    private static final Logger log = Logger.getLogger(Producer.class.getName());
    private static final String TASK_QUEUE_NAME = "task_queue";
    private Channel channel;
    private ConnectionFactory factory;
    private Connection connection;

    /**
     * Establishes a new connection to a RabbitMQ Broker which runs locally. Declares a new channel and queue.
     *
     * @param port port number of the Broker to connect to
     */
    public Producer(String host, int port) throws IOException, TimeoutException {
        factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(TASK_QUEUE_NAME, false, false, false, null);
    }

    /**
     * Publishs a given String to the queue. The default exchange is used.
     *
     * @param message which should be sent
     */
    public void sendMessage(String message) {
        try {
            channel.basicPublish("", TASK_QUEUE_NAME,
                    MessageProperties.TEXT_PLAIN,
                    message.getBytes(StandardCharsets.UTF_8));
            System.out.println("Sent '" + message + "'");
        } catch (IOException e) {
            String warning = "Message could not be delivered.";
            log.log(Level.WARNING, warning, e);
        }
    }

    public void stop() throws IOException {
        log.info("Stopping producer...");
        connection.close();
    }
}
