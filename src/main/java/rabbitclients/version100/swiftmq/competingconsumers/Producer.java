package rabbitclients.version100.swiftmq.competingconsumers;

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

        //according to AMQP 1.0 protocol: "attach" handshake:
        setProducerInstance(
                getSession().createProducer(
                        getExchangeName(), QoS.AT_MOST_ONCE
                )
        );
    }

    /**
     * No exchange preparation needed.
     */
    @Override
    public void prepareMessageExchange() { }
}
