package rabbitclients.version100;

import com.swiftmq.amqp.AMQPContext;
import com.swiftmq.amqp.v100.client.*;
import com.swiftmq.amqp.v100.messaging.AMQPMessage;
import rabbitclients.EnvRabbitMQConfig;
import rabbitclients.RabbitMQConfig;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public abstract class BaseClient {
    private final RabbitMQConfig rabbitMQConfig;
    private java.util.function.Consumer<AMQPMessage> messageHandler;
    private Connection connection;
    private Session session;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public BaseClient(RabbitMQConfig rabbitMQConfig, java.util.function.Consumer<AMQPMessage> messageHandler)
            throws IOException, ConnectionClosedException, UnsupportedProtocolVersionException,
            AuthenticationException, SessionHandshakeException {
        this.rabbitMQConfig = rabbitMQConfig;
        this.messageHandler = messageHandler;
        initializeClient();
    }

    public BaseClient(RabbitMQConfig rabbitMQConfig)
            throws IOException, ConnectionClosedException, UnsupportedProtocolVersionException,
            AuthenticationException, SessionHandshakeException {
        this.rabbitMQConfig = rabbitMQConfig;
        initializeClient();
    }

    public java.util.function.Consumer<AMQPMessage> getMessageHandler() {
        return messageHandler;
    }

    private void initializeClient()
            throws UnsupportedProtocolVersionException, ConnectionClosedException, AuthenticationException,
            IOException, SessionHandshakeException {
        createConnection();
        createSession();
    }

    private void createConnection()
            throws IOException, ConnectionClosedException, UnsupportedProtocolVersionException,
            AuthenticationException {
        connection = new com.swiftmq.amqp.v100.client.Connection(
                new AMQPContext(AMQPContext.CLIENT), rabbitMQConfig.getHost(), rabbitMQConfig.getPort(), false);
        connection.setContainerId("client");
        connection.setIdleTimeout(-1);
        connection.setMaxFrameSize(1024 * 4); //frame size must fit the buffering capacity of sender/receiver
        connection.setExceptionListener(Throwable::printStackTrace);
        connection.connect(); //according to AMQP 1.0 protocol: "open" handshake
    }

    private void createSession() throws ConnectionClosedException, SessionHandshakeException {
        session = connection.createSession(10, 10); //"begin" handshake
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

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    protected RabbitMQConfig getRabbitMQConfig() {
        return rabbitMQConfig;
    }
}
