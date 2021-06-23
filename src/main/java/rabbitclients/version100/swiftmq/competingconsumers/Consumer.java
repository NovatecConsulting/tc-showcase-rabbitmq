package rabbitclients.version100.swiftmq.competingconsumers;

import com.swiftmq.amqp.v100.client.AMQPException;
import com.swiftmq.amqp.v100.client.QoS;
import com.swiftmq.amqp.v100.messaging.AMQPMessage;
import com.swiftmq.amqp.v100.types.AMQPString;
import com.swiftmq.amqp.v100.types.AMQPType;
import rabbitclients.EnvRabbitMQConfig;
import rabbitclients.RabbitMQConfig;
import rabbitclients.ReceiverApplication;
import rabbitclients.version100.swiftmq.AbstractAMQPConsumer;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class Consumer extends AbstractAMQPConsumer {

    public Consumer(RabbitMQConfig rabbitMQConfig, java.util.function.Consumer<String> messageHandler)
            throws IOException {
        super(rabbitMQConfig, toAMQPConsumer(Consumer::extractMessage, messageHandler.andThen(System.out::println)));

        try{
            //according to AMQP 1.0 protocol: "attach" handshake:
            setConsumerInstance(
                    getSession().createConsumer(
                            getExchangeName(), 100, QoS.AT_MOST_ONCE, true, null
                    )
            );
        }catch(AMQPException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    private static java.util.function.Consumer<AMQPMessage> toAMQPConsumer(
            Function<AMQPMessage, String> mapper, java.util.function.Consumer<String> messageHandler) {
        return message -> messageHandler.accept(mapper.apply(message));
    }

    /**
     * Extracts the payload of an AMQP 1.0 message and returns it as String value.
     * @param message
     * @return
     */
    private static String extractMessage(AMQPMessage message) {
        AMQPType type = message.getAmqpValue().getValue();
        if (type instanceof AMQPString) {
            return  ((AMQPString) type).getValue();
        }
        return null;
    }

    /**
     * No exchange preparation needed.
     */
    @Override
    public void prepareMessageExchange() { }

    /**
     * Start and test this consumer as a console application.
     * @param args
     * @throws IOException
     * @throws TimeoutException
     */
    public static void main(String[] args) throws IOException, TimeoutException {
        new ReceiverApplication(worker -> new Consumer(new EnvRabbitMQConfig(), worker)).start();
    }
}
