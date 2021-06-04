package rabbitclients.version100.competingconsumers;

import com.swiftmq.amqp.v100.client.AMQPException;
import com.swiftmq.amqp.v100.client.AuthenticationException;
import com.swiftmq.amqp.v100.client.QoS;
import com.swiftmq.amqp.v100.client.UnsupportedProtocolVersionException;
import rabbitclients.AMQPClient;
import rabbitclients.version100.BaseClient;
import java.io.IOException;

public class Consumer extends BaseClient implements AMQPClient {

    public Consumer(String host, int port, java.util.function.Consumer<String> messageHandler)
            throws UnsupportedProtocolVersionException, AMQPException, AuthenticationException, IOException {
        super(host, port, messageHandler);

        //according to AMQP 1.0 protocol: "attach" handshake:
        setConsumerInstance(
                getSession().createConsumer(
                        getExchangeName(), 100, QoS.AT_MOST_ONCE, true, null
                )
        );
    }
}
