package rabbitclients.version100.swiftmq.competingconsumers

import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.spock.Testcontainers
import rabbitclients.Common
import rabbitclients.EnvRabbitMQConfig
import spock.lang.Shared
import spock.lang.Specification

import java.time.Duration
import java.util.concurrent.LinkedBlockingQueue

@Testcontainers
class CCTest extends Specification {

    @Shared
    RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3")
            .withPluginsEnabled("rabbitmq_amqp1_0")
            .withExposedPorts(5672)
            .withStartupTimeout(Duration.ofMinutes(2))

    def producer, consumer1, consumer2, queue, environment
    def sentMessages = ["M1", "M2", "M3"]
    def common = new Common()

    def "messages were consumed at least once"() {
        given:
        queue = new LinkedBlockingQueue()
        consumer1 = new Consumer(environment, queue::add)
        consumer2 = new Consumer(environment, queue::add)

        when:
        consumer1.consumeMessages()
        consumer2.consumeMessages()

        then:
        def receivedMessages = common.getReceivedMessages(3, Duration.ofSeconds(2), queue)
        sentMessages.size() >= receivedMessages.size()
        receivedMessages.containsAll(sentMessages)
    }

    def "messages were consumed at most once"() {
        given:
        queue = new LinkedBlockingQueue()
        consumer1 = new Consumer(environment, queue::add)
        consumer2 = new Consumer(environment, queue::add)

        when:
        consumer1.consumeMessages()
        consumer2.consumeMessages()

        then:
        def receivedMessages = common.getReceivedMessages(4, Duration.ofSeconds(2), queue)
        receivedMessages.size() <= sentMessages.size()
    }

    def setup() {
        def envMap = new Properties()
        envMap.put("PORT", String.valueOf(rabbitMQContainer.getMappedPort(5672)))
        envMap.put("QUEUE_NAME", "task_queue1")
        envMap.put("EXCHANGE_NAME", "task_exchange")
        environment = new EnvRabbitMQConfig(envMap as Map<String, String>)

        producer = new Producer(environment)
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

