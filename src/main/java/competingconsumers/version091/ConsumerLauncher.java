package competingconsumers.version091;

public class ConsumerLauncher {
    private static final int RABBIT_MQ_PORT = 5672;

    /**
     * Enables to run the Consumer from the command line.
     * A local RabbitMQ broker instance needs to be started beforehand!
     * @param args
     */
    public static void main(String[] args) {
        Consumer consumer = new Consumer(RABBIT_MQ_PORT);
        consumer.consumeMessages();
    }
}

