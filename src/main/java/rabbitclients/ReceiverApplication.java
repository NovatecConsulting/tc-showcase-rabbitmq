package rabbitclients;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class ReceiverApplication {
    private static final Logger log = Logger.getLogger(ReceiverApplication.class.getName());

    private final AMQPConsumer receiver;

    public ReceiverApplication(Builder<java.util.function.Consumer<String>,AMQPConsumer> receiverBuilder) throws IOException, TimeoutException {
        this.receiver = receiverBuilder.build(ReceiverApplication::doWork);
        registerShutdownHook();
    }

    public void start() throws IOException {
        receiver.consumeMessages();
    }

    public void stop() {
        try {
            receiver.stop();
        } catch (InterruptedException | IOException e) {
            log.severe("No grateful termination possible.");
        }
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
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

    public interface Builder<S,D> {
        D build(S source) throws IOException, TimeoutException;
    }
}
