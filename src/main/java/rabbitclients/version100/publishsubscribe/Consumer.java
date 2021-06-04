package rabbitclients.version100.publishsubscribe;

import com.swiftmq.amqp.v100.client.AMQPException;
import com.swiftmq.amqp.v100.client.AuthenticationException;
import com.swiftmq.amqp.v100.client.QoS;
import com.swiftmq.amqp.v100.client.UnsupportedProtocolVersionException;
import rabbitclients.AMQPClient;
import rabbitclients.version100.BaseClient;
import java.io.IOException;

public class Consumer extends BaseClient implements AMQPClient {

    public Consumer(String host, int port, int restPort, java.util.function.Consumer<String> messageHandler)
            throws UnsupportedProtocolVersionException, AMQPException, AuthenticationException, IOException {
        super(host, port, restPort, messageHandler);
        prepareMessageExchange();

        //according to AMQP 1.0 protocol: "attach" handshake:
        setConsumerInstance(
                getSession().createConsumer(
                        getQueueName(), 100, QoS.AT_MOST_ONCE, true, null
                )
        );
    }

    /**
     * Declares a new fanout exchange if it was not already created.
     * Declares a new queue with a random name and a binding between this queue and the exchange.
     * @throws IOException if exchange, queue or binding could not be declared
     */
    @Override
    public void prepareMessageExchange() throws IOException {
        Setup setup = new Setup(getRestPort());
        setup.createExchange(getExchangeName());
        setQueueName(setup.createQueueWithRandomName());
        setup.createBinding(getExchangeName(), getQueueName());
    }
}
