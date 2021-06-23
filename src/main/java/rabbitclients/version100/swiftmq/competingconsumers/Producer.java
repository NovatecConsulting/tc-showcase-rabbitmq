package rabbitclients.version100.swiftmq.competingconsumers;

import com.swiftmq.amqp.v100.client.AMQPException;
import com.swiftmq.amqp.v100.client.AuthenticationException;
import com.swiftmq.amqp.v100.client.QoS;
import com.swiftmq.amqp.v100.client.UnsupportedProtocolVersionException;
import rabbitclients.EnvRabbitMQConfig;
import rabbitclients.RabbitMQConfig;
import rabbitclients.SenderApplication;
import rabbitclients.version100.swiftmq.AbstractAMQPProducer;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Producer extends AbstractAMQPProducer {

    public Producer(RabbitMQConfig rabbitMQConfig) throws IOException {
        super(rabbitMQConfig);

        try {
            //according to AMQP 1.0 protocol: "attach" handshake:
            setProducerInstance(
                    getSession().createProducer(
                            getQueueName(), QoS.AT_MOST_ONCE
                    )
            );
        }catch(AMQPException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * No exchange preparation needed.
     */
    @Override
    public void prepareMessageExchange() { }

    /**
     * Start and test this producer as a console application.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new SenderApplication(new Producer(new EnvRabbitMQConfig())).start();
    }
}
