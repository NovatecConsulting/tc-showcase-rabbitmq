package rabbitclients.version100.swiftmq.publishsubscribe;

import com.swiftmq.amqp.v100.client.AMQPException;
import com.swiftmq.amqp.v100.client.AuthenticationException;
import com.swiftmq.amqp.v100.client.QoS;
import com.swiftmq.amqp.v100.client.UnsupportedProtocolVersionException;
import rabbitclients.RabbitMQConfig;
import rabbitclients.version100.swiftmq.AbstractAMQPProducer;
import java.io.IOException;

public class Producer extends AbstractAMQPProducer {

    public Producer(RabbitMQConfig rabbitMQConfig)
            throws AMQPException, UnsupportedProtocolVersionException, IOException, AuthenticationException {
        super(rabbitMQConfig);
        prepareMessageExchange();

        //according to AMQP 1.0 protocol: "attach" handshake
        //Important: target address needs to be specified like explained in the RabbitMQ AMQP plugin documentation
        setProducerInstance(
                getSession().createProducer(
                        "/exchange/" + getExchangeName(), QoS.AT_MOST_ONCE
                )
        );
    }

    /**
     * Declares a new fanout exchange if it was not already created.
     * @throws IOException the exchange could not be declared
     */
    @Override
    public void prepareMessageExchange() throws IOException {
        Setup setup = new Setup(getRabbitMQConfig());
        setup.createExchange(getExchangeName());
    }
}
