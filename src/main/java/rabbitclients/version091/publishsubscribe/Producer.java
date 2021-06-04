package rabbitclients.version091.publishsubscribe;

import rabbitclients.AMQPClient;
import rabbitclients.version091.BaseClient;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import static com.rabbitmq.client.BuiltinExchangeType.FANOUT;
import static com.rabbitmq.client.MessageProperties.TEXT_PLAIN;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Producer extends BaseClient implements AMQPClient {
    private static final Logger log = Logger.getLogger(Producer.class.getName());

    public Producer(String host, int port) throws IOException, TimeoutException {
        super(host, port);
        prepareMessageExchange();
    }

    /**
     * Sends a String message to a fanout exchange without a routing key.
     * @param message message that should be sent to the broker
     */
    @Override
    public void sendMessage(String message) {
        try {
            getChannel().basicPublish(getExchangeName(), "", TEXT_PLAIN, message.getBytes(UTF_8));
            System.out.println("Sent '" + message + "'");
        } catch (IOException e) {
            log.warning( "Message could not be delivered.");
        }
    }

    /**
     * Declares a new fanout exchange if it was not already created.
     * @throws IOException if exchange could not be declared
     */
    public void prepareMessageExchange() throws IOException {
        getChannel().exchangeDeclare(getExchangeName(), FANOUT);
    }
}
