package rabbitclients.version100.swiftmq.publishsubscribe;

import com.swiftmq.amqp.v100.client.AMQPException;
import com.swiftmq.amqp.v100.client.QoS;
import rabbitclients.EnvRabbitMQConfig;
import rabbitclients.RabbitMQConfig;
import rabbitclients.SenderApplication;
import rabbitclients.version100.swiftmq.AbstractAMQPProducer;
import rabbitclients.version100.Setup;

import java.io.IOException;

public class Producer extends AbstractAMQPProducer {

    public Producer(RabbitMQConfig rabbitMQConfig) throws IOException {
        super(rabbitMQConfig);
        prepareMessageExchange();

        try {
            //according to AMQP 1.0 protocol: "attach" handshake
            //Important: target address needs to be specified like explained in the RabbitMQ AMQP plugin documentation
            setProducerInstance(
                    getSession().createProducer(
                            "/exchange/" + getExchangeName(), QoS.AT_MOST_ONCE
                    )
            );
        }catch (AMQPException e) {
            throw new IOException(e.getMessage(), e);
        }
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

    /**
     * Start and test this producer as a console application.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new SenderApplication(new Producer(new EnvRabbitMQConfig())).start();
    }
}
