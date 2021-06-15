package rabbitclients;

import java.io.IOException;

public interface AMQPConsumer extends Stoppable {
    /**
     * Create channels, exchanges, queues and bindings if necessary.
     * @throws IOException
     */
    void prepareMessageExchange() throws IOException;

    /**
     * Waits for messages to consume or actively pulls for them.
     */
    void consumeMessages() throws IOException;
}
