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

    def producer = new Producer(rabbitMQContainer.getMappedPort(5672))
    def queue = new LinkedBlockingQueue();
    def consumer1 = new Consumer(rabbitMQContainer.getMappedPort(5672), queue::add)
    def consumer2 = new Consumer(rabbitMQContainer.getMappedPort(5672), queue::add)
    def sentMessages = ["M1", "M2", "M3"]

    def"messages were consumed at least once"() {
        setup:
        for(item in sentMessages) {
            producer.sendMessage(item)
        }

        when:
        consumer1.consumeMessages()
        consumer2.consumeMessages()

        then:
        def receivedMessages = getReceivedMessages(3, Duration.ofSeconds(2))
        sentMessages.size() == receivedMessages.size()
        receivedMessages.containsAll(sentMessages)
    }

    def getReceivedMessages(int count, Duration timeoutPerPoll) {
        def allConsumedMessages = new ArrayList();
        for(int i = 0; i < count; i++) {
            allConsumedMessages.add(queue.poll(timeoutPerPoll.toMillis(), TimeUnit.MILLISECONDS)) //will be called back as soon as an object is available
        }
        return allConsumedMessages;
    }
}
