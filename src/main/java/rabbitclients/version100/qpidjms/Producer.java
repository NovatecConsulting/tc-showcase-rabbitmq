package rabbitclients.version100.qpidjms;

import org.apache.qpid.jms.JmsQueue;
import rabbitclients.AMQPProducer;
import rabbitclients.EnvRabbitMQConfig;
import rabbitclients.RabbitMQConfig;
import rabbitclients.SenderApplication;

import javax.jms.*;
import java.io.IOException;

public class Producer extends BaseClient implements AMQPProducer {
    private MessageProducer messageProducer;

    public Producer(RabbitMQConfig rabbitMQConfig) throws IOException {
        super(rabbitMQConfig);
        prepareMessageExchange();
    }

    @Override
    public void sendMessage(String message) throws IOException {
        TextMessage textMessage;
        try {
            textMessage = getSession().createTextMessage(message);
            messageProducer.send(textMessage, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
        } catch (JMSException e) {
            throw new IOException(e.getMessage(), e);
        }
        System.out.println("Sending: " + message);
    }

    /**
     * Sends a String message as plain binary data and not AMQP encoded.
     * @param message
     */
    public void sendUnencodedMessage(String message) throws IOException {
        try {
            BytesMessage bytesMessage = getSession().createBytesMessage();
            bytesMessage.writeBytes(message.getBytes());
            messageProducer.send(bytesMessage);
        }catch(JMSException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public void prepareMessageExchange() throws IOException {
        Destination queue = new JmsQueue(getQueueName());
        try {
            messageProducer = getSession().createProducer(queue);
        } catch (JMSException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    public void stop() throws IOException {
        try {
            getConnection().close();
        } catch (JMSException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * Start and test this producer as a console application.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        new SenderApplication(new Producer(new EnvRabbitMQConfig())).start();
    }
}
