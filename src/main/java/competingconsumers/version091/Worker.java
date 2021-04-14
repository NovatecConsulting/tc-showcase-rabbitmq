package competingconsumers.version091;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Worker {
    private static final String TASK_QUEUE_NAME = "task_queue";

    /**
     * Waits for messages to be assigned to from a subscribed queue. Those messages are then processed
     * according to their complexity.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        final Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();

        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
        System.out.println(" ... Waiting for messages. To exit press CTRL+C");

        //enable fair dispatch: if this consumer is still busy, RabbitMQ will assign the task to the next consumer.
        //Important: if all consumers are busy, the queue will fill up!
        channel.basicQos(1);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");

            System.out.println("Received '" + message + "'");
            try {
                doWork(message);
            } finally {
                System.out.println("Done.");
                //send acknowledgement when finished
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
        //no auto-acknowledgement!
        channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> { });
    }

    /**
     * This function can be used to simulate the complexity of a task.
     * It takes a given string and determines the number of dots contained.
     * The thread is paused for this number of seconds.
     * @param task string to be evaluated
     */
    private static void doWork(String task) {
        for (char ch : task.toCharArray()) {
            if (ch == '.') {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException _ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}

