package rabbitclients;

import com.swiftmq.amqp.v100.client.AMQPException;
import com.swiftmq.amqp.v100.client.AuthenticationException;
import com.swiftmq.amqp.v100.client.UnsupportedProtocolVersionException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ReceiverApplication {
    private static final Logger log = Logger.getLogger(ReceiverApplication.class.getName());
    private static final int RABBIT_MQ_PORT = 5672;
    private static final int RABBIT_MQ_REST_PORT = 15672;
    private static final String HOST = "localhost";
    private static AMQPConsumer receiver;

    public static void main(String[] args)
            throws UnsupportedProtocolVersionException, AMQPException, AuthenticationException, IOException {
        //receiver = new Consumer(HOST, RABBIT_MQ_PORT, RABBIT_MQ_REST_PORT, ReceiverApplication::doWork);
        //start(receiver);
    }

    private static void start(AMQPConsumer receiver) throws IOException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                receiver.stop();
                receiver.getCountDownLatch().await(10, TimeUnit.SECONDS);
            } catch (InterruptedException | IOException e) {
                log.severe("No grateful termination possible.");
            }
        }));
        receiver.consumeMessages();
    }

    /**
     * Simulates the workload of message processing and can be replaced by any other kind of method.
     * Takes a String as input and pauses the thread for the number of dots contained in the String.
     * @param receivedMessage processed message
     */
    private static void doWork(String receivedMessage) {
        for (char ch : receivedMessage.toCharArray()) {
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
