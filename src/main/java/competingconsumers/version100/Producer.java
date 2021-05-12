package competingconsumers.version100;

import com.rabbitmq.client.*;
import org.apache.qpid.proton.amqp.Binary;
import org.apache.qpid.proton.amqp.messaging.Data;
import org.apache.qpid.proton.engine.ProtonJSession;
import org.apache.qpid.proton.engine.impl.SessionImpl;
import org.apache.qpid.proton.message.Message;
import org.apache.qpid.proton.message.impl.MessageImpl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class Producer {
    private static final String TASK_QUEUE_NAME = "task_queue";
    private String connectionUrl = "localhost:5672";

    /**
     * Establishes a new connection to a RabbitMQ Broker which runs locally. Declares a new channel and queue.
     * @param port port number of the Broker to connect to
     */
    public Producer(int port) {

    }

    /**
     * Publishs a given String to the queue. The default exchange is used.
     * @param message which should be sent
     */
    public void sendMessage(Message message) {

    }

    public Message createMessage(String address, String subject, String contentType, Data body) {
        Message message = new MessageImpl();
        message.setAddress(address);
        message.setSubject(subject);
        message.setContentType(contentType);
        message.setBody(body);

        return message;
    }
}
