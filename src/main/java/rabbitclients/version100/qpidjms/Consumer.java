package rabbitclients.version100.qpidjms;

import org.apache.qpid.jms.JmsQueue;
import rabbitclients.AMQPConsumer;
import rabbitclients.RabbitMQConfig;
import javax.jms.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class Consumer extends BaseClient implements AMQPConsumer, Runnable {
    private static final Logger log = Logger.getLogger(Consumer.class.getName());
    private MessageConsumer messageConsumer;
    private AtomicBoolean running = new AtomicBoolean(true);
    private Thread getMessage;

    public Consumer(RabbitMQConfig rabbitMQConfig, java.util.function.Consumer<String> messageHandler)
            throws JMSException {
        super(rabbitMQConfig, messageHandler);
        prepareMessageExchange();
    }

    @Override
    public void prepareMessageExchange() throws JMSException {
        Destination queue = new JmsQueue(getQueueName());
        messageConsumer = getSession().createConsumer(queue);
    }

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
                TextMessage message = (TextMessage) messageConsumer.receive(1000);
                if(message != null) {
                    getMessageHandler().accept(message.getText());
                    System.out.println("Received message");
                }
            }
        } catch (JMSException e) {
            log.warning("Could not consume message.");
        } finally {
            try {
                log.info("Stopping client...");
                getConnection().close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() throws InterruptedException {
        running.set(false);
        getMessage.join();
    }
}
