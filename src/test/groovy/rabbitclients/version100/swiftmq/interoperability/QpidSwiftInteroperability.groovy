package rabbitclients.version100.swiftmq.interoperability

import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.spock.Testcontainers
import rabbitclients.Common
import rabbitclients.EnvRabbitMQConfig
import spock.lang.Shared
import spock.lang.Specification
import java.time.Duration
import java.util.concurrent.LinkedBlockingQueue

/**
 * Test of compatibility/interoperability of SwiftMQ 1.0 and Qpid JMS 1.0 clients.
 */
@Testcontainers
class QpidSwiftInteroperability extends Specification {

    @Shared
    RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3")
            .withPluginsEnabled("rabbitmq_amqp1_0")
            .withExposedPorts(5672, 15672)

    def producer, consumer1, queue, environment
    def sentMessages = ["M1", "M2", "M3"]
    def common = new Common()

    def "consumed messages equal sent messages for SwiftMQ Producer and Qpid Consumer"() {
        given:
        producer = new rabbitclients.version100.swiftmq.competingconsumers.Producer(environment)
        for (item in sentMessages) {
            producer.sendMessage(item)
        }

        queue = new LinkedBlockingQueue()
        consumer1 = new rabbitclients.version100.qpidjms.Consumer(environment, queue::add)

        when:
        consumer1.consumeMessages()

        then:
        def receivedMessages = common.getReceivedMessages(3, Duration.ofSeconds(2), queue)
        sentMessages.size() >= receivedMessages.size()
        receivedMessages.containsAll(sentMessages)
    }

    def "consumed messages equal sent messages for Qpid Producer and SwiftMQ Consumer"() {
        given:
        producer = new rabbitclients.version100.qpidjms.Producer(environment)
        for (item in sentMessages) {
            producer.sendMessage(item)
        }

        queue = new LinkedBlockingQueue()
        consumer1 = new rabbitclients.version100.swiftmq.competingconsumers.Consumer(environment, queue::add)

        when:
        consumer1.consumeMessages()

        then:
        def receivedMessages = common.getReceivedMessages(3, Duration.ofSeconds(2), queue)
        sentMessages.size() >= receivedMessages.size()
        receivedMessages.containsAll(sentMessages)
    }

    def setup() {
        def envMap = new Properties()
        envMap.put("PORT", String.valueOf(rabbitMQContainer.getMappedPort(5672)))
        envMap.put("QUEUE_NAME", "task_queue1")
        envMap.put("EXCHANGE_NAME", "task_exchange")
        environment = new EnvRabbitMQConfig(envMap as Map<String, String>)
    }

    def cleanup() {
        consumer1.stop()
        producer.stop()
    }
}

