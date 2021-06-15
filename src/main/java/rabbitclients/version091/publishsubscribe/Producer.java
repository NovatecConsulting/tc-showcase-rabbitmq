package rabbitclients.version091.publishsubscribe;

import rabbitclients.AMQPProducer;
import rabbitclients.RabbitMQConfig;
import rabbitclients.Stoppable;
import rabbitclients.version091.BaseClient;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import static com.rabbitmq.client.BuiltinExchangeType.FANOUT;
import static com.rabbitmq.client.MessageProperties.TEXT_PLAIN;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Producer extends BaseClient implements AMQPProducer, Stoppable {
    private static final Logger log = Logger.getLogger(Producer.class.getName());

    public Producer(RabbitMQConfig rabbitMQConfig) throws IOException, TimeoutException {
        super(rabbitMQConfig);
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
    @Override
    public void prepareMessageExchange() throws IOException {
        getChannel().exchangeDeclare(getExchangeName(), FANOUT);
    }

    @Override
    public void stop() throws IOException {
        log.info("Stopping client...");
        if(getConnection() != null) {
            getConnection().close();
            getCountDownLatch().countDown();
        }else {
            log.severe("Connection could not be closed because it was never established.");
        }
    }
}
