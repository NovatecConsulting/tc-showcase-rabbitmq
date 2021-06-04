package rabbitclients.version100;

import com.swiftmq.amqp.AMQPContext;
import com.swiftmq.amqp.v100.client.*;
import com.swiftmq.amqp.v100.generated.messaging.message_format.AmqpValue;
import com.swiftmq.amqp.v100.messaging.AMQPMessage;
import com.swiftmq.amqp.v100.types.AMQPString;
import com.swiftmq.amqp.v100.types.AMQPType;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public abstract class BaseClient {
    private static final String TASK_EXCHANGE_NAME = "task_exchange";
    private static final Logger log = Logger.getLogger(BaseClient.class.getName());
    private final String host;
    private final int port;
    private int restPort;
    private String queueName, messageValue;
    private java.util.function.Consumer<String> messageHandler;
    private Connection connection;
    private Session session;
    private Consumer consumerInstance;
    private Producer producerInstance;
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private AtomicBoolean running = new AtomicBoolean(true);

    public BaseClient(String host, int port, int restPort, java.util.function.Consumer<String> messageHandler)
            throws IOException, ConnectionClosedException, UnsupportedProtocolVersionException,
            AuthenticationException, SessionHandshakeException {
        this.messageHandler = messageHandler;
        this.host = host;
        this.port = port;
        this.restPort = restPort;
        initializeClient();
    }

    public BaseClient(String host, int port, java.util.function.Consumer<String> messageHandler)
            throws IOException, ConnectionClosedException, UnsupportedProtocolVersionException,
            AuthenticationException, SessionHandshakeException {
        this.messageHandler = messageHandler;
        this.host = host;
        this.port = port;
        initializeClient();
    }

    public BaseClient(String host, int port, int restPort)
            throws IOException, ConnectionClosedException, UnsupportedProtocolVersionException,
            AuthenticationException, SessionHandshakeException {
        this.host = host;
        this.port = port;
        this.restPort = restPort;
        initializeClient();
    }

    public BaseClient(String host, int port)
            throws IOException, ConnectionClosedException, UnsupportedProtocolVersionException,
            AuthenticationException, SessionHandshakeException {
        this.host = host;
        this.port = port;
        initializeClient();
    }

    private void initializeClient()
            throws UnsupportedProtocolVersionException, ConnectionClosedException, AuthenticationException,
            IOException, SessionHandshakeException {
        createConnection();
        createSession();
    }

    public void createConnection()
            throws IOException, ConnectionClosedException, UnsupportedProtocolVersionException,
            AuthenticationException {
        connection = new com.swiftmq.amqp.v100.client.Connection(
                new AMQPContext(AMQPContext.CLIENT), host, port, false);
        connection.setContainerId("client");
        connection.setIdleTimeout(-1);
        connection.setMaxFrameSize(1024 * 4); //frame size must fit the buffering capacity of sender/receiver
        connection.setExceptionListener(Throwable::printStackTrace);
        connection.connect(); //according to AMQP 1.0 protocol: "open" handshake
    }

    public void createSession() throws ConnectionClosedException, SessionHandshakeException {
        session = connection.createSession(10, 10); //"begin" handshake
    }

    /**
     * Consumes messages in while-loop.
     */
    public void consumeMessages() {
        try {
            while (running.get()) {
                getMessage();
            }
        } finally {
            log.info("Stopping client...");
            getConnection().close();
            getCountDownLatch().countDown();
        }
    }

    /**
     * Polls for messages with a timeout duration. If a message is available, it is processed and settled.
     */
    private void getMessage() {
        if(consumerInstance != null) {
            AMQPMessage message = consumerInstance.receive(TimeUnit.SECONDS.toSeconds(1));
            if (message != null) {
                String s = determineMessageVersion(message);

                getMessageHandler().accept(s);
                System.out.println("Received " + s);

                settleMessage(message);
            }
        }
    }

    /**
     * Determines the AMQP version of the arriving message. The message payload is extracted according
     * to the protocol version.
     * @param message the AMQP message to be processed
     * @return the extracted and decoded message payload
     */
    private String determineMessageVersion(AMQPMessage message) {
        if (message.getAmqpValue() != null) {
            AMQPType type = message.getAmqpValue().getValue();
            if (type instanceof AMQPString) {
                messageValue = ((AMQPString) type).getValue();
            }
        } else {
            messageValue = new String(message.getData().get(0).getValue());
        }
        return messageValue;
    }

    /**
     * Settles ("acknowledges") the message if necessary.
     * @param message
     */
    private void settleMessage(AMQPMessage message) {
        if (!message.isSettled()) { //only settlement needed if message was not sent as "fire-and-forget"
            try {
                message.accept();
            } catch (InvalidStateException e) {
                log.severe("Consumed message was in an invalid state. Unexpected behavior!");
            }
        }
    }

    /**
     * Sends a message as AMQPValue in AMQP 1.0 format to the RabbitMQ broker.
     * Important: the exact target address is specified in the inheriting class!
     * @param message
     */
    public void sendMessage(String message) {
        try {
            AMQPMessage msg = new AMQPMessage(); //default-values are set for header-fields and properties
            System.out.println("Sending: " + message);
            msg.setAmqpValue(new AmqpValue(new AMQPString(message)));
            producerInstance.send(msg);
        } catch (AMQPException e) {
            log.warning("Message could not get delivered.");
        }
    }

    public void stop() {
        running.set(false);
    }

    public void prepareMessageExchange() throws IOException { }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public Session getSession() {
        return session;
    }

    public Connection getConnection() {
        return connection;
    }

    public String getQueueName() {
        return queueName;
    }

    public String getExchangeName() {
        return TASK_EXCHANGE_NAME;
    }

    public int getRestPort() {
        return restPort;
    }

    public java.util.function.Consumer<String> getMessageHandler() {
        return messageHandler;
    }

    public void setConsumerInstance(Consumer consumer) {
        this.consumerInstance = consumer;
    }

    public void setProducerInstance(Producer producer) {
        this.producerInstance = producer;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
}
