package competingconsumers.version091

import competingconsumers.version091.polling.Consumer
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@Testcontainers
class PollingDeliveryTest extends Specification {

    @Shared
    RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3")
            .withExposedPorts(5672)

    def producer, consumer1, consumer2, queue
    def sentMessages = ["M1", "M2", "M3"]

    def "messages were consumed at least once"() {
        given:
        queue = new LinkedBlockingQueue()
        consumer1 = new Consumer("localhost", rabbitMQContainer.getMappedPort(5672), queue::add)
        consumer2 = new Consumer("localhost", rabbitMQContainer.getMappedPort(5672), queue::add)

        when:
        startConsumerAsynchron(consumer1)
        startConsumerAsynchron(consumer2)

        then:
        def receivedMessages = getReceivedMessages(3, Duration.ofSeconds(2), queue)
        sentMessages.size() >= receivedMessages.size()
        receivedMessages.containsAll(sentMessages)
    }

    def "messages were consumed at most once"() {
        given:
        queue = new LinkedBlockingQueue()
        consumer1 = new Consumer("localhost", rabbitMQContainer.getMappedPort(5672), queue::add)
        consumer2 = new Consumer("localhost", rabbitMQContainer.getMappedPort(5672), queue::add)

        when:
        startConsumerAsynchron(consumer1)
        startConsumerAsynchron(consumer2)

        then:
        def receivedMessages = getReceivedMessages(4, Duration.ofSeconds(2), queue)
        receivedMessages.size() <= sentMessages.size()
    }

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

    def startConsumerAsynchron(Consumer consumer) {
        new Thread(() -> consumer.consumeMessages()).start()
    }

    def setup() {
        producer = new Producer("localhost", rabbitMQContainer.getMappedPort(5672))
        for (item in sentMessages) {
            producer.sendMessage(item)
        }
    }

    def cleanup() {
        consumer1.stop()
        consumer2.stop()
        producer.stop()
    }
}
