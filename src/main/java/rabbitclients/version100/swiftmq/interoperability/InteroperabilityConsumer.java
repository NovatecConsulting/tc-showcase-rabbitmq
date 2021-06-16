package rabbitclients.version100.swiftmq.interoperability;

import com.swiftmq.amqp.v100.client.AMQPException;
import com.swiftmq.amqp.v100.client.AuthenticationException;
import com.swiftmq.amqp.v100.client.QoS;
import com.swiftmq.amqp.v100.client.UnsupportedProtocolVersionException;
import com.swiftmq.amqp.v100.messaging.AMQPMessage;
import com.swiftmq.amqp.v100.types.AMQPString;
import com.swiftmq.amqp.v100.types.AMQPType;
import rabbitclients.RabbitMQConfig;
import rabbitclients.version100.swiftmq.AbstractAMQPConsumer;
import java.io.IOException;
import java.util.function.Function;
import static java.nio.charset.StandardCharsets.UTF_8;

public class InteroperabilityConsumer extends AbstractAMQPConsumer {

    public InteroperabilityConsumer(RabbitMQConfig rabbitMQConfig, java.util.function.Consumer<String> messageHandler)
            throws UnsupportedProtocolVersionException, AMQPException, AuthenticationException, IOException {
        super(rabbitMQConfig, toAMQPConsumer(InteroperabilityConsumer::extractMessage, messageHandler.andThen(System.out::println)));
        prepareMessageExchange();

        //according to AMQP 1.0 protocol: "attach" handshake:
        setConsumerInstance(
                getSession().createConsumer(
                        getQueueName(), 100, QoS.AT_MOST_ONCE, true, null
                )
        );
    }

    private static  java.util.function.Consumer<AMQPMessage> toAMQPConsumer(Function<AMQPMessage, String> mapper, java.util.function.Consumer<String> messageHandler) {
        return message -> messageHandler.accept(mapper.apply(message));
    }

    /**
     * Determines the AMQP version of the arriving message. The message payload is extracted according
     * to the protocol version.
     * @param message the AMQP message to be processed
     * @return the extracted and decoded message payload
     */
    private static String extractMessage(AMQPMessage message) {
        String messageValue = null;
        if (message.getAmqpValue() != null) {
            AMQPType type = message.getAmqpValue().getValue();
            if (type instanceof AMQPString) {
                messageValue = ((AMQPString) type).getValue();
            }
        } else {
            messageValue = new String(message.getData().get(0).getValue(), UTF_8);
        }
        return messageValue;
    }


    /**
     * No exchange preparation needed.
     */
    @Override
    public void prepareMessageExchange() { }
}

