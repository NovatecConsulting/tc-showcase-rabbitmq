package rabbitclients.version091;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public abstract class BaseClient {
    private static final String TASK_QUEUE_NAME = "task_queue";
    private static final String TASK_EXCHANGE_NAME = "task_exchange";
    private static final Logger log = Logger.getLogger(BaseClient.class.getName());
    private final String host;
    private final int port;
    private java.util.function.Consumer<String> messageHandler;
    private Channel channel;
    private Connection connection;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public BaseClient(String host, int port, java.util.function.Consumer<String> messageHandler) throws IOException, TimeoutException {
        this.messageHandler = messageHandler;
        this.host = host;
        this.port = port;
        initializeClient();
    }

    public BaseClient(String host, int port) throws IOException, TimeoutException {
        this.host = host;
        this.port = port;
        initializeClient();
    }

    private void initializeClient() throws IOException, TimeoutException {
        createConnection();
        createSession();
        prepareMessageExchange();
    }

    public void createConnection() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        connection = factory.newConnection();
    }

    public void createSession() throws IOException {
        if(connection != null) {
            channel = connection.createChannel();
        }
    }

    public void stop() throws IOException {
        log.info("Stopping client...");
        connection.close();
        countDownLatch.countDown();
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getQueueName() {
        return TASK_QUEUE_NAME;
    }

    public String getExchangeName() {
        return TASK_EXCHANGE_NAME;
    }

    public java.util.function.Consumer<String> getMessageHandler() {
        return messageHandler;
    }

    public abstract void prepareMessageExchange() throws IOException;

    public void consumeMessages() { }

    public void sendMessage(String message) { }
}
