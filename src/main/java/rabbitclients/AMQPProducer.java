package rabbitclients;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.io.IOException;

public interface AMQPProducer extends Stoppable {
    /**
     * Create channels, exchanges, queues and bindings if necessary.
     * @throws IOException
     */
    void prepareMessageExchange() throws IOException, NamingException, JMSException;

    /**
     * Sends a given String as AMQP message to the broker. The message can be in AMQP 0.9.1 or 1.0 format, but
     * compatibility can not be ensured in both directions.
     * @param message to be sent
     */
    void sendMessage(String message) throws JMSException;
}
