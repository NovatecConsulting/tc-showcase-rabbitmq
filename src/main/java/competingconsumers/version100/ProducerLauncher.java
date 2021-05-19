package competingconsumers.version100;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProducerLauncher {
    private static final int RABBIT_MQ_PORT = 5672;

    /**
     * Enables to run the Producer from the command line.
     * A local RabbitMQ broker instance needs to be started beforehand!
     * Input is taken from the command line and is then forwarded for publishing.
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        competingconsumers.version100.Producer producer = new competingconsumers.version100.Producer(RABBIT_MQ_PORT);
        AtomicBoolean running = new AtomicBoolean(true);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { running.set(false); }));

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

