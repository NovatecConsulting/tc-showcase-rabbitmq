package rabbitclients.version100.swiftmq.publishsubscribe

import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.spock.Testcontainers
import rabbitclients.Common
import rabbitclients.EnvRabbitMQConfig
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

    def publisher, subscriber1, subscriber2, queue, environment1, environment2, environment3
    def sentMessages = ["M1", "M2", "M3"]
    def subscriber1Queue = new LinkedBlockingQueue()
    def subscriber2Queue = new LinkedBlockingQueue()
    def common = new Common()

    def "messages were consumed at least once"() {
        given:
        queue = new LinkedBlockingQueue()

        subscriber1 = new Consumer(environment1, subscriber1Queue::add)
        subscriber2 = new Consumer(environment2, subscriber2Queue::add)
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
        subscriber1 = new Consumer(environment1, subscriber1Queue::add)
        subscriber2 = new Consumer(environment2, subscriber2Queue::add)
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
        def envMap1 = new Properties()
        envMap1.put("PORT", String.valueOf(rabbitMQContainer.getMappedPort(5672)))
        envMap1.put("MANAGEMENT_PORT", String.valueOf(rabbitMQContainer.getMappedPort(15672)))
        envMap1.put("QUEUE_NAME", "task_queue1")
        envMap1.put("EXCHANGE_NAME", "task_exchange")
        environment1 = new EnvRabbitMQConfig(envMap1 as Map<String, String>)

        def envMap2 = new Properties()
        envMap2.put("PORT", String.valueOf(rabbitMQContainer.getMappedPort(5672)))
        envMap2.put("MANAGEMENT_PORT", String.valueOf(rabbitMQContainer.getMappedPort(15672)))
        envMap2.put("QUEUE_NAME", "task_queue2")
        envMap2.put("EXCHANGE_NAME", "task_exchange")
        environment2 = new EnvRabbitMQConfig(envMap2 as Map<String, String>)

        def envMap3 = new Properties();
        envMap3.put("PORT", String.valueOf(rabbitMQContainer.getMappedPort(5672)))
        envMap3.put("MANAGEMENT_PORT", String.valueOf(rabbitMQContainer.getMappedPort(15672)))
        envMap3.put("QUEUE_NAME", "task_queue3")
        envMap3.put("EXCHANGE_NAME", "task_exchange")
        environment3 = new EnvRabbitMQConfig(envMap3 as Map<String, String>)

        publisher = new Producer(environment3)
    }

    def cleanup() {
        subscriber1.stop()
        subscriber2.stop()
        publisher.stop()
    }
}

