package rabbitclients.version100;

import com.swiftmq.amqp.v100.client.*;
import com.swiftmq.amqp.v100.messaging.AMQPMessage;
import rabbitclients.AMQPConsumer;
import rabbitclients.RabbitMQConfig;
import rabbitclients.Stoppable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public abstract class AbstractAMQPConsumer extends BaseClient implements AMQPConsumer, Stoppable {
    private static final Logger log = Logger.getLogger(BaseClient.class.getName());
    private AtomicBoolean running = new AtomicBoolean(true);
    private Consumer consumerInstance;

    public AbstractAMQPConsumer(RabbitMQConfig rabbitMQConfig, java.util.function.Consumer<AMQPMessage> messageHandler)
            throws UnsupportedProtocolVersionException, SessionHandshakeException, ConnectionClosedException,
            AuthenticationException, IOException {
        super(rabbitMQConfig, messageHandler);
    }

    /**
     * Consumes messages using a while-loop.
     */
    @Override
    public void consumeMessages() {
        try {
            while (running.get()) {
                receiveMessage();
            }
        } finally {
            log.info("Stopping client...");
            if(getConnection() != null) {
                getConnection().close();
                getCountDownLatch().countDown();
            }else {
                log.severe("Connection could not be closed because it was never established.");
            }
        }
    }

    @Override
    public void stop() {
        running.set(false);
    }

    /**
     * Polls for messages with a timeout duration. If a message is available, it is processed and settled.
     */
    private void receiveMessage() {
        if(consumerInstance != null) {
            AMQPMessage message = consumerInstance.receive(TimeUnit.SECONDS.toSeconds(1));
            if (message != null) {
                getMessageHandler().accept(message);
                System.out.println("Received message");
                settleMessage(message);
            }
        }
    }

    /**
     * Settles ("acknowledges") the message if necessary.
     * @param message
     */
    private void settleMessage(AMQPMessage message) {
        if (!message.isSettled()) { //only settlement needed if message was not sent as "fire-and-forget"
            try {
                message.accept();
            } catch (InvalidStateException e) {
                log.severe("Consumed message was in an invalid state. Unexpected behavior!");
            }
        }
    }

    protected void setConsumerInstance(Consumer consumer) {
        this.consumerInstance = consumer;
    }
}
