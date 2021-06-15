package rabbitclients;

import java.io.IOException;

public interface AMQPProducer extends Stoppable {
    /**
     * Create channels, exchanges, queues and bindings if necessary.
     * @throws IOException
     */
    void prepareMessageExchange() throws IOException;

    /**
     * Sends a given String as AMQP message to the broker.
     * @param message to be sent
     */
    void sendMessage(String message);
}
