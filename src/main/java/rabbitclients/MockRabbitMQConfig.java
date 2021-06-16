package rabbitclients;

/**
 * Mocks RabbitMQ configurations that are usually needed for tests.
 * Port, port of the management API, queue names and exchange names need to be injectable
 * because they are dependent from the test scenario.
 */
public class MockRabbitMQConfig implements RabbitMQConfig{
    private static final String HOST = "localhost";
    private static final String USER = "guest";
    private static final String PASSWORD = "guest";
    private final int PORT;
    private final int MANAGEMENT_PORT;
    private final String QUEUE_NAME;
    private final String EXCHANGE_NAME;

    public MockRabbitMQConfig(int port, int managementPort, String queueName, String exchangeName) {
        PORT = port;
        MANAGEMENT_PORT = managementPort;
        QUEUE_NAME = queueName;
        EXCHANGE_NAME = exchangeName;
    }

    @Override
    public String getHost() {
        return HOST;
    }

    @Override
    public int getPort() {
        return PORT;
    }

    @Override
    public int getManagementPort() {
        return MANAGEMENT_PORT;
    }

    @Override
    public String getUser() {
        return USER;
    }

    @Override
    public String getPassword() {
        return PASSWORD;
    }

    @Override
    public String getQueueName() {
        return QUEUE_NAME;
    }

    @Override
    public String getExchangeName() {
        return EXCHANGE_NAME;
    }
}
