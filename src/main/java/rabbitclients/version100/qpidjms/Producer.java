package rabbitclients.version100.qpidjms;

import org.apache.qpid.jms.JmsQueue;
import rabbitclients.AMQPProducer;
import rabbitclients.RabbitMQConfig;
import javax.jms.*;

public class Producer extends BaseClient implements AMQPProducer {
    private MessageProducer messageProducer;

    public Producer(RabbitMQConfig rabbitMQConfig) throws JMSException {
        super(rabbitMQConfig);
        prepareMessageExchange();
    }

    @Override
    public void sendMessage(String message) throws JMSException {
        TextMessage textMessage = getSession().createTextMessage(message);
        messageProducer.send(textMessage, DeliveryMode.NON_PERSISTENT, Message.DEFAULT_PRIORITY, Message.DEFAULT_TIME_TO_LIVE);
        System.out.println("Sending: " + message);
    }

    @Override
    public void prepareMessageExchange() throws JMSException {
        Destination queue = new JmsQueue(getQueueName());
        messageProducer = getSession().createProducer(queue);
    }

    @Override
    public void stop() throws JMSException {
        getConnection().close();
    }
}
