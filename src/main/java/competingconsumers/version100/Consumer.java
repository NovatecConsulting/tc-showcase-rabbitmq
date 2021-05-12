package competingconsumers.version100;

import org.apache.qpid.proton.engine.Connection;

public class Consumer {
    private static final String TASK_QUEUE_NAME = "task_queue";
    private Connection connection;
    private java.util.function.Consumer<String> messageHandler;

    /**
     * Establishes a new connection to a RabbitMQ Broker which runs locally. Declares a new channel and queue.
     * @param port port number of the Broker to connect to
     */
    public Consumer(int port, java.util.function.Consumer<String> messageHandler) {

    }

    /**
     * Implements the process of message consumption: waits for messages, triggers processing
     * and sends an acknowledgement.
     */
    public void consumeMessages() {

    }
}
