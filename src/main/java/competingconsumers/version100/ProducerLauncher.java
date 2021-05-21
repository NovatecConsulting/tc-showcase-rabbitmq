package competingconsumers.version100;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class ProducerLauncher {
    private static final Logger log = Logger.getLogger(Consumer.class.getName());
    private static final int RABBIT_MQ_PORT = 5672;
    private static final String HOST = "localhost";

    /**
     * Enables to run the Producer from the command line.
     * A local RabbitMQ broker instance needs to be started beforehand!
     * Input is taken from the command line and is then forwarded for publishing.
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        Producer producer = new Producer(HOST, RABBIT_MQ_PORT);
        AtomicBoolean running = new AtomicBoolean(true);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            producer.stop();
            try {
                producer.getCountDownLatch().await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.severe("No grateful termination possible.");
            }
        }
        ));

        //wait for input from the console
        while (running.get()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter a message. The number of dots contained denotes the complexity. 'Exit' will stop the program.");
            String message = br.readLine();
            if(message.equals("Exit")) {
                running.set(false);
            }else {
                producer.sendMessage(message);
            }
        }
        producer.stop();
    }
}

