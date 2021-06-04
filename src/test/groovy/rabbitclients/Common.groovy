package rabbitclients

import java.time.Duration
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class Common {
    def getReceivedMessages(int count, Duration timeoutPerPoll, LinkedBlockingQueue queue) {
        def allConsumedMessages = new ArrayList()
        for(int i = 0; i < count; i++) {
            def message = queue.poll(timeoutPerPoll.toMillis(), TimeUnit.MILLISECONDS)
            if(message != null) {
                allConsumedMessages.add(message)
            }
        }
        return allConsumedMessages
    }

    def startConsumerAsynchron(AMQPClient consumer) {
        new Thread(() -> consumer.consumeMessages()).start()
    }
}
