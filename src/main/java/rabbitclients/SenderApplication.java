package rabbitclients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class SenderApplication {
    private static final Logger log = Logger.getLogger(SenderApplication.class.getName());
    private static final int RABBIT_MQ_PORT = 5672;
    private static final int RABBIT_MQ_REST_PORT = 15672;
    private static final String HOST = "localhost";
    private static AMQPProducer sender;
    private static AtomicBoolean running = new AtomicBoolean(true);

    public static void main(String[] args) throws Exception {
        //sender = new Producer(HOST, RABBIT_MQ_PORT, RABBIT_MQ_REST_PORT);
        //start(sender);
    }

    private static void start(AMQPProducer sender) throws IOException, InterruptedException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                sender.stop();
                sender.getCountDownLatch().await(10, TimeUnit.SECONDS);
            } catch (InterruptedException | IOException e) {
                log.severe("No grateful termination possible.");
            }
        }
        ));
        readMessages();
    }

    /**
     * Reads input from the console and sends it to the message broker.
     * @throws IOException if the input cannot be read
     */
    private static void readMessages() throws IOException, InterruptedException {
        while (running.get()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter a message. The number of dots contained denotes the complexity. 'Exit' will stop the program.");
            String message = br.readLine();
            if (message.equals("Exit")) {
                running.set(false);
            } else {
                sender.sendMessage(message);
            }
        }
        sender.stop();
    }
}

