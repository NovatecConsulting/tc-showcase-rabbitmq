package rabbitclients.version100.qpidjms;

import org.apache.qpid.jms.JmsConnectionFactory;
import rabbitclients.RabbitMQConfig;
import javax.jms.*;

public abstract class BaseClient {
    private java.util.function.Consumer<String> messageHandler;
    private RabbitMQConfig rabbitMQConfig;
    private Connection connection;
    private Session session;

    public BaseClient(RabbitMQConfig rabbitMQConfig, java.util.function.Consumer<String> messageHandler)
            throws JMSException {
        this.messageHandler = messageHandler;
        this.rabbitMQConfig = rabbitMQConfig;
        initialize();
    }

    public BaseClient(RabbitMQConfig rabbitMQConfig) throws JMSException {
        this.rabbitMQConfig = rabbitMQConfig;
        initialize();
    }

    private void initialize() throws JMSException {
        createConnection();
        createSession();
    }

    private void createConnection() throws JMSException {
        String uri = "amqp://" + rabbitMQConfig.getHost() + ":" + rabbitMQConfig.getPort();
        ConnectionFactory factory = new JmsConnectionFactory(rabbitMQConfig.getUser(), rabbitMQConfig.getPassword(), uri);
        connection = factory.createConnection(rabbitMQConfig.getUser(), rabbitMQConfig.getPassword());
        connection.start();
    }

    private void createSession() throws JMSException {
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    public Connection getConnection() {
        return connection;
    }

    public Session getSession() {
        return session;
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
}
