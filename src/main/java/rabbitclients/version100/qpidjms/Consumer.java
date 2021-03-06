package rabbitclients.version100.qpidjms;

import org.apache.qpid.jms.JmsQueue;
import org.apache.qpid.jms.message.JmsBytesMessage;
import rabbitclients.AMQPConsumer;
import rabbitclients.EnvRabbitMQConfig;
import rabbitclients.RabbitMQConfig;
import rabbitclients.ReceiverApplication;
import javax.jms.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class Consumer extends BaseClient implements AMQPConsumer, Runnable {
    private static final Logger log = Logger.getLogger(Consumer.class.getName());
    private MessageConsumer messageConsumer;
    private AtomicBoolean running = new AtomicBoolean(true);
    private Thread getMessage;

    public Consumer(RabbitMQConfig rabbitMQConfig, java.util.function.Consumer<String> messageHandler)
            throws IOException {
        super(rabbitMQConfig, messageHandler);
        prepareMessageExchange();
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
                Message message = messageConsumer.receive(1000);
                if(message != null) {
                    if(message instanceof TextMessage) {
                        getMessageHandler().accept(((TextMessage)message).getText());
                        System.out.println("Received message " + ((TextMessage)message).getText());
                    } else if(message instanceof JmsBytesMessage) {
                        JmsBytesMessage bytesMessage = (JmsBytesMessage) message;
                        byte[] b = new byte[(int)bytesMessage.getBodyLength()];
                        bytesMessage.readBytes(b);
                        String text = new String(b, StandardCharsets.UTF_8);
                        getMessageHandler().accept(text);
                        System.out.println("Received message " + text);
                    } else {
                        log.severe("Type of received message is unknown.");
                    }
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
    public void prepareMessageExchange() throws IOException {
        Destination queue = new JmsQueue(getQueueName());
        try {
            messageConsumer = getSession().createConsumer(queue);
        } catch (JMSException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public void stop() throws InterruptedException {
        running.set(false);
        getMessage.join();
    }

    /**
     * Start and test this consumer as a console application.
     * @param args
     * @throws IOException
     * @throws TimeoutException
     */
    public static void main(String[] args) throws IOException, TimeoutException {
        new ReceiverApplication(worker -> new Consumer(new EnvRabbitMQConfig(), worker)).start();
    }

    /**
     * Start and test this producer as a console application.
     * @param config
     * @throws IOException
     * @throws TimeoutException
     */
    public static void main(RabbitMQConfig config) throws IOException, TimeoutException {
        new ReceiverApplication(worker -> new Consumer(config, worker)).start();
    }
}
