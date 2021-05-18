package competingconsumers.version091

import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@Testcontainers
class DeliveryTest extends Specification {

    @Shared
    RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3")
            .withExposedPorts(5672)

    def sentMessages = ["M1", "M2", "M3"]
    def producer, consumer1, consumer2
    def consumer1Queue = new LinkedBlockingQueue()
    def consumer2Queue = new LinkedBlockingQueue()
    def queue = new LinkedBlockingQueue()


    def"messages were consumed at least once"() {
        setup:
        consumer1 = new Consumer(rabbitMQContainer.getMappedPort(5672), queue::add)
        consumer2 = new Consumer(rabbitMQContainer.getMappedPort(5672), queue::add)

        when:
        consumer1.consumeMessages()
        consumer2.consumeMessages()

        then:
        def receivedMessages = getReceivedMessages(3, Duration.ofSeconds(2), queue)
        sentMessages.size() == receivedMessages.size()
        receivedMessages.containsAll(sentMessages)
    }

    def"messages were consumed at most once"() {
        setup:
        consumer1 = new Consumer(rabbitMQContainer.getMappedPort(5672), queue::add)
        consumer2 = new Consumer(rabbitMQContainer.getMappedPort(5672), queue::add)

        when:
        consumer1.consumeMessages()
        consumer2.consumeMessages()

        then:
        def receivedMessages = getReceivedMessages(4, Duration.ofSeconds(2), queue)
        receivedMessages.size() <= sentMessages.size()
    }

    def"messages were distributed to all consumers"() {
        setup:
        consumer1 = new Consumer(rabbitMQContainer.getMappedPort(5672), consumer1Queue::add)
        consumer2 = new Consumer(rabbitMQContainer.getMappedPort(5672), consumer2Queue::add)

        when:
        consumer1.consumeMessages()
        consumer2.consumeMessages()

        then:
        def receivedMessagesConsumer1 = getReceivedMessages(2, Duration.ofSeconds(2), consumer1Queue)
        !receivedMessagesConsumer1.isEmpty()

        def receivedMessagesConsumer2 = getReceivedMessages(2, Duration.ofSeconds(2), consumer2Queue)
        !receivedMessagesConsumer2.isEmpty()
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

    def setup() {
        producer = new Producer(rabbitMQContainer.getMappedPort(5672))
        for(item in sentMessages) {
            producer.sendMessage(item)
        }
    }

    def cleanup() {
        consumer1.stop()
        consumer2.stop()
        producer.stop()
    }
}
