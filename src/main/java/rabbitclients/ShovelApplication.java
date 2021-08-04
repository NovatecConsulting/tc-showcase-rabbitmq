package rabbitclients;

import rabbitclients.version100.qpidjms.Consumer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class ShovelApplication {
    private static final Logger log = Logger.getLogger(ShovelApplication.class.getName());
    private static final String SRC_PROTOCOL = "amqp091";
    private static final String DEST_PROTOCOL = "amqp10";
    private static final String SRC_ADDRESS = "task_queue"; //queue specification for AMQP 0.9.1
    private static final String DEST_ADDRESS = "/amq/queue/shovel_queue"; //endpoint specification for AMQP 1.0

    /**
     * Creates a shovel (source AMQP 0.9.1, destination AMQP 1.0) and consumes the messages using a
     * Qpid AMQP 1.0 consumer.
     * @param args
     */
    public static void main(String[] args) {
        try {
            //prepare Shovel configuration
            RabbitMQConfig consumerConfig = setupRabbitMQConfig("shovel_queue");
            ShovelConfig shovelConfig = new EnvShovelConfig(consumerConfig, SRC_PROTOCOL, DEST_PROTOCOL,
                    SRC_ADDRESS, DEST_ADDRESS);
            shovelConfig.setupShovel();

            //start AMQP 1.0 Consumer
            Consumer.main(consumerConfig);
        }catch(IOException | TimeoutException e) {
            log.severe("Error in Shovel configuration and consumer startup.");
        }
    }

    public static RabbitMQConfig setupRabbitMQConfig(String queueName) {
        Map<String, String> config = new HashMap<>();
        config.put("QUEUE_NAME", queueName);
        return new EnvRabbitMQConfig(config);
    }
}
