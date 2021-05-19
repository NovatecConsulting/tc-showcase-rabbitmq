package competingconsumers.version100;

import com.swiftmq.amqp.AMQPContext;
import com.swiftmq.amqp.v100.client.*;
import com.swiftmq.amqp.v100.messaging.AMQPMessage;
import com.swiftmq.amqp.v100.types.AMQPString;
import com.swiftmq.amqp.v100.types.AMQPType;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Consumer {
    private static final String TASK_QUEUE_NAME = "task_queue";
    private final Connection connection;
    private final Session session;
    private final com.swiftmq.amqp.v100.client.Consumer consumerInstance;
    private final java.util.function.Consumer<String> messageHandler;
    private AtomicBoolean running = new AtomicBoolean(true);

    /**
     * Establishes a new connection to a RabbitMQ Broker which runs locally and sets some basic properties.
     * Creates a session on this connection and a producer.
     *
     * @param port port number of the Broker to connect to
     */
    public Consumer(int port, java.util.function.Consumer<String> messageHandler) throws UnsupportedProtocolVersionException, AMQPException, AuthenticationException, IOException {
        this.messageHandler = messageHandler;

        int qos = QoS.AT_MOST_ONCE;
        AMQPContext ctx = new AMQPContext(AMQPContext.CLIENT);

        connection = new Connection(ctx, "127.0.0.1", port, false);
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
        consumerInstance = session.createConsumer(TASK_QUEUE_NAME, 100, qos, true, null); //"attach" handshake
    }

    /**
     * Consumes messages infinitely until connection is closed.
     */
    public void consumeMessages() {
        try {
            while (running.get()) {
                getMessage();
            }
        } finally {
            connection.close();
        }
    }

    /**
     * Checks if a messages is available that can be consumed. If this is the case, the message
     * is processed and an acknowledgement is sent.
     */
    private void getMessage() {
        try {
            //wait for messages with timeout
            AMQPMessage message = consumerInstance.receive(TimeUnit.SECONDS.toSeconds(1));
            if (message != null) {
                final AMQPType value = message.getAmqpValue().getValue();
                if (value instanceof AMQPString) {
                    String s = ((AMQPString) value).getValue();
                    System.out.println("Received: " + s);
                    messageHandler.accept(s);
                    System.out.println("Done");
                }
                if (!message.isSettled()) //only settlement needed if message was not sent as "fire-and-forget"
                    message.accept();
            }
        } catch (AMQPException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        running.set(false);
    }
}
