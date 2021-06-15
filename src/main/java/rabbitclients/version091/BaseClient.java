package rabbitclients.version091;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import rabbitclients.RabbitMQConfig;
import rabbitclients.Stoppable;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public abstract class BaseClient {
    private RabbitMQConfig rabbitMQConfig;
    private java.util.function.Consumer<String> messageHandler;
    private Channel channel;
    private Connection connection;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public BaseClient(RabbitMQConfig rabbitMQConfig, java.util.function.Consumer<String> messageHandler)
            throws IOException, TimeoutException {
        this.rabbitMQConfig = rabbitMQConfig;
        this.messageHandler = messageHandler;
        initializeClient();
    }

    public BaseClient(RabbitMQConfig rabbitMQConfig) throws IOException, TimeoutException {
        this.rabbitMQConfig = rabbitMQConfig;
        initializeClient();
    }

    private void initializeClient() throws IOException, TimeoutException {
        createConnection();
        createSession();
        prepareMessageExchange();
    }

    public void createConnection() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitMQConfig.getHost());
        factory.setPort(rabbitMQConfig.getPort());
        connection = factory.newConnection();
    }

    public void createSession() throws IOException {
        if(connection != null) {
            channel = connection.createChannel();
        }
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public Channel getChannel() {
        return channel;
    }

    public Connection getConnection() {
        return connection;
    }

    public String getQueueName() {
        return rabbitMQConfig.getQueueName();
    }

    public String getExchangeName() {
        return rabbitMQConfig.getExchangeName();
    }

    public java.util.function.Consumer<String> getMessageHandler() {
        return messageHandler;
    }

    protected abstract void prepareMessageExchange() throws IOException;
}
