package rabbitclients;

import java.io.IOException;

public interface AMQPConsumer extends Stoppable {
    /**
     * Create channels, exchanges, queues and bindings if necessary.
     * @throws IOException
     */
    void prepareMessageExchange() throws IOException;

    /**
     * Opens a new thread for message consumption and waits for new messages (event-driven) or
     * actively polls for new messages (polling).
     */
    void consumeMessages() throws IOException;
}
