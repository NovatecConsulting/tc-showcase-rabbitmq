package competingconsumers.version100;

import com.swiftmq.amqp.AMQPContext;
import com.swiftmq.amqp.v100.client.*;
import com.swiftmq.amqp.v100.messaging.AMQPMessage;
import com.swiftmq.amqp.v100.types.AMQPString;
import com.swiftmq.amqp.v100.types.AMQPType;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Consumer {
    private static final String TASK_QUEUE_NAME = "task_queue";
    private Connection connection;
    private Session session;
    private com.swiftmq.amqp.v100.client.Consumer consumerInstance;
    private java.util.function.Consumer<String> messageHandler;

    /**
     * Establishes a new connection to a RabbitMQ Broker which runs locally and sets some basic properties.
     * Creates a session on this connection and a producer.
     * @param port port number of the Broker to connect to
     */
    public Consumer(int port, java.util.function.Consumer<String> messageHandler) {
        try {
            this.messageHandler = messageHandler;

            int qos = QoS.AT_MOST_ONCE;
            AMQPContext ctx = new AMQPContext(AMQPContext.CLIENT);

            connection = new Connection(ctx, "127.0.0.1", port, false);
            connection.setContainerId("client");
            connection.setIdleTimeout(-1);
            connection.setMaxFrameSize(1024 * 4);
            connection.setExceptionListener(Throwable::printStackTrace);
            connection.connect();

            session = connection.createSession(10, 10);
            consumerInstance = session.createConsumer(TASK_QUEUE_NAME, 100, qos, true, null);
        } catch (IOException | UnsupportedProtocolVersionException | AuthenticationException | AMQPException e) {
            e.printStackTrace();
        }
    }

    /**
     * Implements the process of message consumption: waits for messages, triggers processing
     * and sends an acknowledgement.
     */
    public void consumeMessages() {
        while(true) {
            try {
                //blocking implementation of poll() with a timeout, for non-blocking use receiveNoWait()
                //(there is no message listener like in JMS available)
                AMQPMessage message = consumerInstance.receive(TimeUnit.SECONDS.toSeconds(1));
                if (message != null) {
                    final AMQPType value = message.getAmqpValue().getValue();
                    if (value instanceof AMQPString) {
                        String s = ((AMQPString) value).getValue();
                        System.out.println("Received: " + s);
                        messageHandler.accept(s);
                        System.out.println("Done");
                    }
                    if (!message.isSettled())
                        message.accept();

                }
            } catch (AMQPException e) {
                e.printStackTrace();
            }
        }
    }
}
