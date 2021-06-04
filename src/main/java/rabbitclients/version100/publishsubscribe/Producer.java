package rabbitclients.version100.publishsubscribe;

import com.swiftmq.amqp.v100.client.AMQPException;
import com.swiftmq.amqp.v100.client.AuthenticationException;
import com.swiftmq.amqp.v100.client.QoS;
import com.swiftmq.amqp.v100.client.UnsupportedProtocolVersionException;
import rabbitclients.AMQPClient;
import rabbitclients.version100.BaseClient;
import java.io.IOException;
import java.util.logging.Logger;

public class Producer extends BaseClient implements AMQPClient {
    private static final Logger log = Logger.getLogger(Producer.class.getName());

    public Producer(String host, int port, int restPort)
            throws AMQPException, UnsupportedProtocolVersionException, IOException, AuthenticationException {
        super(host, port, restPort);
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

    @Override
    public void stop() {
        log.info("Stopping producer...");
        getConnection().close();
        getCountDownLatch().countDown();
    }
}
