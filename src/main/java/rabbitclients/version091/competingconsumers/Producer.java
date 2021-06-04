package rabbitclients.version091.competingconsumers;

import rabbitclients.AMQPClient;
import rabbitclients.version091.BaseClient;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import static com.rabbitmq.client.MessageProperties.TEXT_PLAIN;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Producer extends BaseClient implements AMQPClient {
    private static final Logger log = Logger.getLogger(Producer.class.getName());

    public Producer(String host, int port) throws IOException, TimeoutException {
        super(host, port);
        prepareMessageExchange();
    }

    /**
     * Sends a String message to the default exchange using the queue name as routing key.
     * @param message message that should be sent to the broker
     */
    @Override
    public void sendMessage(String message) {
        try {
            getChannel().basicPublish("", getQueueName(), TEXT_PLAIN, message.getBytes(UTF_8));
            System.out.println("Sent '" + message + "'");
        } catch (IOException e) {
            log.warning( "Message could not be delivered.");
        }
    }

    /**
     * Declare a new queue on this session/channel if the queue was not already created.
     * @throws IOException if queue could not be declared
     */
    public void prepareMessageExchange() throws IOException {
        getChannel().queueDeclare(getQueueName(), false, false, false, null);
    }
}
