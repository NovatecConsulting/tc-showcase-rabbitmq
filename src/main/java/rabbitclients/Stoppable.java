package rabbitclients;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public interface Stoppable {
    /**
     * Gracefully stops the client and closes the connection to the broker.
     * @throws IOException
     */
    void stop() throws IOException, InterruptedException;

    CountDownLatch getCountDownLatch();
}
