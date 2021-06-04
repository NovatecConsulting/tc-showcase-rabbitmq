package rabbitclients.version100

import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.spock.Testcontainers
import rabbitclients.Common
import rabbitclients.version100.publishsubscribe.Consumer
import rabbitclients.version100.publishsubscribe.Producer
import spock.lang.Shared
import spock.lang.Specification
import java.time.Duration
import java.util.concurrent.LinkedBlockingQueue

@Testcontainers
class PubSubTest extends Specification {

    @Shared
    RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3")
            .withPluginsEnabled("rabbitmq_amqp1_0", "rabbitmq_management")
            .withExposedPorts(5672, 15672)

    def publisher, subscriber1, subscriber2, queue
    def sentMessages = ["M1", "M2", "M3"]
    def subscriber1Queue = new LinkedBlockingQueue()
    def subscriber2Queue = new LinkedBlockingQueue()
    def common = new Common()

    def "messages were consumed at least once"() {
        given:
        queue = new LinkedBlockingQueue()
        subscriber1 = new Consumer("localhost", rabbitMQContainer.getMappedPort(5672), rabbitMQContainer.getMappedPort(15672), subscriber1Queue::add)
        subscriber2 = new Consumer("localhost", rabbitMQContainer.getMappedPort(5672), rabbitMQContainer.getMappedPort(15672), subscriber2Queue::add)
        for (item in sentMessages) {
            publisher.sendMessage(item)
        }

        when:
        common.startConsumerAsynchron(subscriber1)
        common.startConsumerAsynchron(subscriber2)

        then:
        checkSentGreaterEqualsReceived(subscriber1Queue)
        checkSentGreaterEqualsReceived(subscriber2Queue)
    }

    def "messages were consumed at most once"() {
        given:
        queue = new LinkedBlockingQueue()
        subscriber1 = new Consumer("localhost", rabbitMQContainer.getMappedPort(5672), rabbitMQContainer.getMappedPort(15672), subscriber1Queue::add)
        subscriber2 = new Consumer("localhost", rabbitMQContainer.getMappedPort(5672), rabbitMQContainer.getMappedPort(15672), subscriber2Queue::add)
        for (item in sentMessages) {
            publisher.sendMessage(item)
        }

        when:
        common.startConsumerAsynchron(subscriber1)
        common.startConsumerAsynchron(subscriber2)

        then:
        checkSentLessEqualsReceived(subscriber1Queue)
        checkSentLessEqualsReceived(subscriber2Queue)
    }

    def checkSentLessEqualsReceived(LinkedBlockingQueue queue) {
        def receivedMessages = common.getReceivedMessages(3, Duration.ofSeconds(2), queue)
        if ((sentMessages.size() <= receivedMessages.size()) && receivedMessages.containsAll(sentMessages)) {
            return true
        }else {
            return false
        }
    }

    def checkSentGreaterEqualsReceived(LinkedBlockingQueue queue) {
        def receivedMessages = common.getReceivedMessages(3, Duration.ofSeconds(2), queue)
        if ((sentMessages.size() >= receivedMessages.size()) && receivedMessages.containsAll(sentMessages)) {
            return true
        }else {
            return false
        }
    }

    def setup() {
        publisher = new Producer("localhost", rabbitMQContainer.getMappedPort(5672), rabbitMQContainer.getMappedPort(15672))
    }

    def cleanup() {
        subscriber1.stop()
        subscriber2.stop()
        publisher.stop()
    }
}

