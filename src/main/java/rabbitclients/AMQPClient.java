package rabbitclients;

import com.swiftmq.amqp.v100.client.AuthenticationException;
import com.swiftmq.amqp.v100.client.ConnectionClosedException;
import com.swiftmq.amqp.v100.client.SessionHandshakeException;
import com.swiftmq.amqp.v100.client.UnsupportedProtocolVersionException;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public interface AMQPClient {
    /**
     * Opens a new Connection to the RabbitMQ Broker.
     * @throws IOException
     * @throws ConnectionClosedException
     * @throws UnsupportedProtocolVersionException
     * @throws AuthenticationException
     * @throws TimeoutException
     */
    void createConnection() throws IOException, ConnectionClosedException, UnsupportedProtocolVersionException, AuthenticationException, TimeoutException;

    /**
     * Creates a session on the beforehand opened connection.
     * @throws ConnectionClosedException
     * @throws SessionHandshakeException
     * @throws IOException
     */
    void createSession() throws ConnectionClosedException, SessionHandshakeException, IOException;

    /**
     * Create channels, exchanges, queues and bindings if necessary.
     * @throws IOException
     */
    void prepareMessageExchange() throws IOException;

    /**
     * Sends a given String as AMQP message to the broker.
     * @param message
     */
    void sendMessage(String message);

    /**
     * Waits for messages to consume or actively pulls for them.
     */
    void consumeMessages();

    /**
     * Gracefully stops the client and closes the connection to the broker.
     * @throws IOException
     */
    void stop() throws IOException;

    /**
     * Returns the CountDownLatch that might be needed for thread joining and graceful connection closing.
     * @return
     */
    CountDownLatch getCountDownLatch();
}
