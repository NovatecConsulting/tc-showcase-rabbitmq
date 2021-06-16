package rabbitclients.version100.swiftmq.publishsubscribe

import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.spock.Testcontainers
import rabbitclients.Common
import rabbitclients.MockRabbitMQConfig
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
    def mappedPort = rabbitMQContainer.getMappedPort(5672)
    def mappedManagementPort = rabbitMQContainer.getMappedPort(15672)
    def mockEnvironment1 = new MockRabbitMQConfig(mappedPort, mappedManagementPort,"task_queue1", "task_exchange")
    def mockEnvironment2 = new MockRabbitMQConfig(mappedPort, mappedManagementPort,"task_queue2", "task_exchange")
    def mockEnvironment3 = new MockRabbitMQConfig(mappedPort, mappedManagementPort,"task_queue3", "task_exchange")

    def "messages were consumed at least once"() {
        given:
        queue = new LinkedBlockingQueue()

        subscriber1 = new Consumer(mockEnvironment1, subscriber1Queue::add)
        subscriber2 = new Consumer(mockEnvironment2, subscriber2Queue::add)
        for (item in sentMessages) {
            publisher.sendMessage(item)
        }

        when:
        subscriber1.consumeMessages()
        subscriber2.consumeMessages()

        then:
        checkSentGreaterEqualsReceived(subscriber1Queue)
        checkSentGreaterEqualsReceived(subscriber2Queue)
    }

    def "messages were consumed at most once"() {
        given:
        queue = new LinkedBlockingQueue()
        subscriber1 = new Consumer(mockEnvironment1, subscriber1Queue::add)
        subscriber2 = new Consumer(mockEnvironment2, subscriber2Queue::add)
        for (item in sentMessages) {
            publisher.sendMessage(item)
        }

        when:
        subscriber1.consumeMessages()
        subscriber2.consumeMessages()

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
        publisher = new Producer(mockEnvironment3)
    }

    def cleanup() {
        subscriber1.stop()
        subscriber2.stop()
        publisher.stop()
    }
}

