package rabbitclients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class SenderApplication {
    private static final Logger log = Logger.getLogger(SenderApplication.class.getName());
    private final AMQPProducer sender;
    private AtomicBoolean running = new AtomicBoolean(true);

    public SenderApplication(AMQPProducer sender) {
        this.sender = sender;
        registerShutdownHook();
    }

    public void start() throws IOException {
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
    }

    public void stop() {
        try {
            sender.stop();
        } catch (InterruptedException | IOException e) {
            log.severe("No grateful termination possible.");
        }
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }
}
