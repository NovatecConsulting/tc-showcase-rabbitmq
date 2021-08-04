package rabbitclients;

import rabbitclients.version100.Setup;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EnvShovelConfig implements ShovelConfig{
    private static final String SRC_PROTOCOL = "SRC_PROTOCOL";
    private static final String DEST_PROTOCOL = "DEST_PROTOCOL";
    private static final String SRC_ADDRESS = "SRC_ADDRESS";
    private static final String DEST_ADDRESS = "DEST_ADDRESS";

    private final Map<String, String> shovelConfig;
    private final RabbitMQConfig rabbitMQConfig;

    public EnvShovelConfig(RabbitMQConfig rabbitMQConfig) {
        this(rabbitMQConfig, System.getenv());
    }

    public EnvShovelConfig(RabbitMQConfig rabbitMQConfig, Map<String, String> shovelConfig) {
        this.rabbitMQConfig = rabbitMQConfig;
        this.shovelConfig = shovelConfig;
    }

    public EnvShovelConfig(RabbitMQConfig rabbitMQConfig, String srcProtocol, String destProtocol, String srcAddress,
                           String destAddress) {
        this.rabbitMQConfig = rabbitMQConfig;
        shovelConfig = new HashMap<>();
        shovelConfig.put("SRC_PROTOCOL", srcProtocol);
        shovelConfig.put("DEST_PROTOCOL", destProtocol);
        shovelConfig.put("SRC_ADDRESS", srcAddress);
        shovelConfig.put("DEST_ADDRESS", destAddress);
    }

    @Override
    public String getSrcProtocol() {
        return shovelConfig.getOrDefault(SRC_PROTOCOL, "amqp091");
    }

    @Override
    public String getDestProtocol() {
        return shovelConfig.getOrDefault(DEST_PROTOCOL, "amqp091");
    }

    @Override
    public String getSrcAddress() {
        return shovelConfig.getOrDefault(SRC_ADDRESS, "amqp://localhost:5672");
    }

    @Override
    public String getDestAddress() {
        return shovelConfig.getOrDefault(DEST_ADDRESS, "amqp://localhost:5672");
    }

    @Override
    public void setupShovel() throws IOException {
        Setup setup = new Setup(rabbitMQConfig);
        setup.createQueue(rabbitMQConfig.getQueueName());
        setup.createShovelConfig(getSrcProtocol(), getDestProtocol(), getSrcAddress(), getDestAddress());
    }
}
