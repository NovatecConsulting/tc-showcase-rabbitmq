package rabbitclients.version100.swiftmq;

import com.swiftmq.amqp.v100.client.*;
import com.swiftmq.amqp.v100.messaging.AMQPMessage;
import rabbitclients.AMQPConsumer;
import rabbitclients.RabbitMQConfig;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public abstract class AbstractAMQPConsumer extends BaseClient implements AMQPConsumer, Runnable {
    private static final Logger log = Logger.getLogger(AbstractAMQPConsumer.class.getName());
    private AtomicBoolean running = new AtomicBoolean(true);
    private Consumer consumerInstance;
    private Thread getMessage;

    public AbstractAMQPConsumer(RabbitMQConfig rabbitMQConfig, java.util.function.Consumer<AMQPMessage> messageHandler)
            throws IOException {
        super(rabbitMQConfig, messageHandler);
    }

    /**
     * Starts consumption of new messages in a new thread.
     */
    @Override
    public void consumeMessages() {
        System.out.println(" ... Waiting for messages. To exit press CTRL+C");
        getMessage = new Thread(this); //get messages in polling behaviour in new thread
        getMessage.start();
    }

    /**
     * Consumes new messages in a while-loop.
     */
    @Override
    public void run() {
        try {
            while (running.get()) {
                receiveMessage();
            }
        } finally {
            log.info("Stopping client...");
            getConnection().close();
        }
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

    /**
     * Stop the message consumption while-loop and join the thread.
     * @throws InterruptedException
     */
    @Override
    public void stop() throws InterruptedException {
        running.set(false);
        getMessage.join();
    }

    protected void setConsumerInstance(Consumer consumer) {
        this.consumerInstance = consumer;
    }

    protected abstract void prepareMessageExchange() throws IOException;
}
