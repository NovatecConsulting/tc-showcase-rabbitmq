package competingconsumers.version100;

import com.swiftmq.amqp.AMQPContext;
import com.swiftmq.amqp.v100.client.*;
import com.swiftmq.amqp.v100.generated.messaging.message_format.AmqpValue;
import com.swiftmq.amqp.v100.messaging.AMQPMessage;
import com.swiftmq.amqp.v100.types.AMQPString;

import java.io.IOException;

public class Producer {
    private static final String TASK_QUEUE_NAME = "task_queue";
    private static final String dataFormat = "%01024d";
    private Connection connection;
    private Session session;
    private com.swiftmq.amqp.v100.client.Producer producerInstance;

    /**
     * Establishes a new connection to a RabbitMQ Broker which runs locally. Declares a new channel and queue.
     * @param port port number of the Broker to connect to
     */
    public Producer(int port) {
        try {
            int qos = QoS.AT_MOST_ONCE; //Quality of Service = delivery guarantee
            AMQPContext ctx = new AMQPContext(AMQPContext.CLIENT); //alternative ROUTER is for internal use only

            connection = new Connection(ctx, "localhost", port, false);
            connection.setContainerId("client");
            connection.setIdleTimeout(-1);
            connection.setMaxFrameSize(1024 * 4);
            connection.setExceptionListener(Throwable::printStackTrace);
            connection.connect();

            session = connection.createSession(10, 10);
            producerInstance = session.createProducer(TASK_QUEUE_NAME, qos);
        } catch (IOException | UnsupportedProtocolVersionException | AuthenticationException | AMQPException e) {
            e.printStackTrace();
        }
    }

    /**
     * Publishs a given String to the queue. The default exchange is used.
     * @param message which should be sent
     */
    public void sendMessage(String message) {
        try {
            AMQPMessage msg = new AMQPMessage(); //default-values are set for header-fields and properties
            System.out.println("Sending: " + message);
            msg.setAmqpValue(new AmqpValue(new AMQPString(message)));
            producerInstance.send(msg);
        }catch (AMQPException e) {
            e.printStackTrace();
        }
    }
}
