package rabbitclients.version100.competingconsumers;

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

    public Producer(String host, int port)
            throws AMQPException, UnsupportedProtocolVersionException, IOException, AuthenticationException {
        super(host, port);

        //according to AMQP 1.0 protocol: "attach" handshake:
        setProducerInstance(
                getSession().createProducer(
                        getExchangeName(), QoS.AT_MOST_ONCE
                )
        );
    }

    @Override
    public void stop() {
        log.info("Stopping producer...");
        getConnection().close();
        getCountDownLatch().countDown();
    }
}
