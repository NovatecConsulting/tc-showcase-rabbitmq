package competingconsumers.version091.polling;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class ConsumerLauncher {
    private static final Logger log = Logger.getLogger(Consumer.class.getName());
    private static final int RABBIT_MQ_PORT = 5672;
    private static final String HOST = "localhost";

    /**
     * Enables to run the Consumer from the command line.
     * A local RabbitMQ broker instance needs to be started beforehand!
     * @param args
     */
    public static void main(String[] args) throws IOException, TimeoutException {
        Consumer consumer = new Consumer(HOST, RABBIT_MQ_PORT, ConsumerLauncher::doWork);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            consumer.stop();
            try {
                consumer.getCountDownLatch().await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.severe("No grateful termination possible.");
            }
        }
        ));
        consumer.consumeMessages();
    }

    /**
     * Can be used to simulate the complexity of a task.
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

