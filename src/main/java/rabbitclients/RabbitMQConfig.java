package rabbitclients;

import java.util.List;

public interface RabbitMQConfig {

    String getHost();

    int getPort();

    int getManagementPort();

    String getUser();

    String getPassword();

    String getQueueName();

    String getExchangeName();
}
