package competingconsumers.version091;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Producer {
    private static final String TASK_QUEUE_NAME = "task_queue";
    private Channel channel;
    private ConnectionFactory factory;
    private Connection connection;

    /**
     * Establishes a new connection to a RabbitMQ Broker which runs locally. Declares a new channel and queue.
     * @param port port number of the Broker to connect to
     */
    public Producer(int port) {
        try {
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
     * Publishs a given String to the queue. The default exchange is used.
     * @param message which should be sent
     */
    public void sendMessage(String message) {
        try {
            channel.basicPublish("", TASK_QUEUE_NAME,
                    MessageProperties.TEXT_PLAIN,
                    message.getBytes(StandardCharsets.UTF_8));
            System.out.println("Sent '" + message + "'");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
