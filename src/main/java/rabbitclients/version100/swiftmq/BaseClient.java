package rabbitclients.version100.swiftmq;

import com.swiftmq.amqp.AMQPContext;
import com.swiftmq.amqp.v100.client.*;
import com.swiftmq.amqp.v100.messaging.AMQPMessage;
import rabbitclients.RabbitMQConfig;
import java.io.IOException;

public abstract class BaseClient {
    private final RabbitMQConfig rabbitMQConfig;
    private java.util.function.Consumer<AMQPMessage> messageHandler;
    private Connection connection;
    private Session session;

    public BaseClient(RabbitMQConfig rabbitMQConfig, java.util.function.Consumer<AMQPMessage> messageHandler) throws IOException {
        this.rabbitMQConfig = rabbitMQConfig;
        this.messageHandler = messageHandler;
        initializeClient();
    }

    public BaseClient(RabbitMQConfig rabbitMQConfig) throws IOException {
        this(rabbitMQConfig, null);
    }

    private void initializeClient() throws IOException {
        createConnection();
        createSession();
    }

    private void createConnection() throws IOException {
        try {
            connection = new com.swiftmq.amqp.v100.client.Connection(
                    new AMQPContext(AMQPContext.CLIENT), rabbitMQConfig.getHost(), rabbitMQConfig.getPort(), false);
            connection.setContainerId("client");
            connection.setIdleTimeout(-1);
            connection.setMaxFrameSize(1024 * 4); //frame size must fit the buffering capacity of sender/receiver
            connection.setExceptionListener(Throwable::printStackTrace);
            connection.connect(); //according to AMQP 1.0 protocol: "open" handshake
        }catch (IOException | ConnectionClosedException | UnsupportedProtocolVersionException |
                AuthenticationException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    private void createSession() throws IOException {
        try {
            session = connection.createSession(10, 10); //"begin" handshake
        }catch (ConnectionClosedException | SessionHandshakeException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public java.util.function.Consumer<AMQPMessage> getMessageHandler() {
        return messageHandler;
    }

    protected Session getSession() {
        return session;
    }

    protected Connection getConnection() {
        return connection;
    }

    protected String getQueueName() {
        return rabbitMQConfig.getQueueName();
    }

    protected String getExchangeName() {
        return rabbitMQConfig.getExchangeName();
    }

    protected RabbitMQConfig getRabbitMQConfig() {
        return rabbitMQConfig;
    }
}
