package competingconsumers.version091;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Producer {

    private static final String TASK_QUEUE_NAME = "task_queue";

    /**
     * Waits for input from the console and sends it as a message to the queue.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            //in order to enable durability, the queue and the messages need to be marked as persistent
            channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);

            //wait for input from the console
            while(true) {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                System.out.println("Enter a message. The number of dots contained denotes the complexity:");
                String message = br.readLine();

                //send message to the queue and mark them as persistent
                channel.basicPublish("", TASK_QUEUE_NAME,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        message.getBytes("UTF-8"));
                System.out.println("Sent '" + message + "'");
            }
        }
    }
}

