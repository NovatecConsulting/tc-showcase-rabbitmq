package rabbitclients.version100.swiftmq;

import com.swiftmq.amqp.v100.client.*;
import com.swiftmq.amqp.v100.generated.messaging.message_format.AmqpValue;
import com.swiftmq.amqp.v100.messaging.AMQPMessage;
import com.swiftmq.amqp.v100.types.AMQPString;
import rabbitclients.AMQPProducer;
import rabbitclients.RabbitMQConfig;
import java.io.IOException;
import java.util.logging.Logger;

public abstract class AbstractAMQPProducer extends BaseClient implements AMQPProducer {
    private com.swiftmq.amqp.v100.client.Producer producerInstance;
    private static final Logger log = Logger.getLogger(AbstractAMQPProducer.class.getName());

    public AbstractAMQPProducer(RabbitMQConfig rabbitMQConfig) throws IOException {
        super(rabbitMQConfig);
    }

    /**
     * Sends a message as AMQPValue in AMQP 1.0 format to the RabbitMQ broker.
     * Important: the exact target address is specified in the inheriting class!
     * @param message
     */
    @Override
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

    @Override
    public void stop() {
        log.info("Stopping client...");
        getConnection().close();
    }

    protected void setProducerInstance(Producer producer) {
        this.producerInstance = producer;
    }
}
