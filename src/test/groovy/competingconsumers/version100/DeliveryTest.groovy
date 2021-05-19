package competingconsumers.version100

import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.spock.Testcontainers
import org.testcontainers.utility.DockerImageName
import spock.lang.Shared
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@Testcontainers
class DeliveryTest extends Specification {

    @Shared
    RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3")
            .withPluginsEnabled("rabbitmq_amqp1_0")
            .withExposedPorts(5672)

    def producer = new Producer(rabbitMQContainer.getMappedPort(5672))
    def queue = new LinkedBlockingQueue();
    def consumer1 = new Consumer(rabbitMQContainer.getMappedPort(5672), queue::add)
    def consumer2 = new Consumer(rabbitMQContainer.getMappedPort(5672), queue::add)
    def sentMessages = ["M1", "M2", "M3"]

    def "messages were consumed exactly once"() {
        when:
        startConsumerAsynchron(consumer1)
        startConsumerAsynchron(consumer2)

        then:
        def receivedMessages = getReceivedMessages(3, Duration.ofSeconds(2))
        sentMessages.size() == receivedMessages.size()
        receivedMessages.containsAll(sentMessages)
    }

    def getReceivedMessages(int count, Duration timeoutPerPoll) {
        def allConsumedMessages = new ArrayList();
        for (int i = 0; i < count; i++) {
            def message = queue.poll(timeoutPerPoll.toMillis(), TimeUnit.MILLISECONDS)
            if (message != null) {
                allConsumedMessages.add(message)
            }
        }
        return allConsumedMessages;
    }

    def startConsumerAsynchron(Consumer consumer) {
        new Thread(() -> consumer.consumeMessages()).start()
    }

    def setup() {
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
