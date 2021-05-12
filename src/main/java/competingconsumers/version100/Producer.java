package competingconsumers.version100;

import com.swiftmq.amqp.AMQPContext;
import com.swiftmq.amqp.v100.client.*;
import com.swiftmq.amqp.v100.generated.messaging.message_format.AmqpValue;
import com.swiftmq.amqp.v100.messaging.AMQPMessage;
import com.swiftmq.amqp.v100.types.AMQPBinary;
import com.swiftmq.amqp.v100.types.AMQPString;

import java.io.IOException;

public class Producer {
    private static final String TASK_QUEUE_NAME = "task_queue";
    private Connection connection;
    private Session session;
    private com.swiftmq.amqp.v100.client.Producer producerInstance;

    /**
     * Establishes a new connection to a RabbitMQ Broker which runs locally and sets some basic properties.
     * Creates a session on this connection and a producer.
     *
     * @param port port number of the Broker to connect to
     */
    public Producer(int port) {
        try {
            int qos = QoS.AT_MOST_ONCE; //Quality of Service = delivery guarantee; EXACTLY_ONCE not supported by RabbitMQ!
            AMQPContext ctx = new AMQPContext(AMQPContext.CLIENT); //alternative ROUTER is for internal use only

            connection = new Connection(ctx, "localhost", port, false);
            connection.setContainerId("client");
            connection.setIdleTimeout(-1);
            connection.setMaxFrameSize(1024 * 4); //make sure that frame size fits the buffering capacity of sender/receiver
            connection.setExceptionListener(Throwable::printStackTrace);
            connection.connect(); //"open" handshake

            /*
            - Sessions bind two one-directional channels together into a bi-directional transfer
            - Connections can be multiplexed using sessions
                -> multiple sessions on one connection
            - Messages can be split into multiple transfers. Those frames will be buffered in a session window.
                -> windowSize = maximum number of unsettled messages/frames
            */
            session = connection.createSession(10, 10); //"begin" handshake
            producerInstance = session.createProducer(TASK_QUEUE_NAME, qos); //"attach" handshake
        } catch (IOException | UnsupportedProtocolVersionException | AuthenticationException | AMQPException e) {
            e.printStackTrace();
        }
    }

    /**
     * Publishs a given String to the queue. Headers and properties are set internally (if not specified explicitly).
     *
     * @param message which should be sent
     */
    public void sendMessage(String message) {
        try {
            AMQPMessage msg = new AMQPMessage(); //default-values are set for header-fields and properties
            System.out.println("Sending: " + message);
            msg.setAmqpValue(new AmqpValue(new AMQPString(message)));
            producerInstance.send(msg);
        } catch (AMQPException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        connection.close();
    }
}
