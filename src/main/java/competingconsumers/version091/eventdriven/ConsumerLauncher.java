package competingconsumers.version091.eventdriven;

public class ConsumerLauncher {
    private static final int RABBIT_MQ_PORT = 5672;

    /**
     * Enables to run the Consumer from the command line.
     * A local RabbitMQ broker instance needs to be started beforehand!
     * @param args
     */
    public static void main(String[] args) {
        Consumer consumer = new Consumer(RABBIT_MQ_PORT, ConsumerLauncher::doWork);
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

