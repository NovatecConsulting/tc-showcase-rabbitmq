package rabbitclients.version091

import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.spock.Testcontainers
import rabbitclients.Common
import rabbitclients.EnvRabbitMQConfig
import rabbitclients.version091.competingconsumers.Producer
import rabbitclients.version091.competingconsumers.eventdriven.Consumer
import spock.lang.Shared
import spock.lang.Specification
import java.time.Duration
import java.util.concurrent.LinkedBlockingQueue

import static rabbitclients.EnvRabbitMQConfig.EXCHANGE_NAME_VAR
import static rabbitclients.EnvRabbitMQConfig.PORT_VAR
import static rabbitclients.EnvRabbitMQConfig.QUEUE_NAME_VAR

@Testcontainers
class CCEventdrivenTest extends Specification {

    @Shared
    RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3")
            .withExposedPorts(5672)
            .withStartupTimeout(Duration.ofMinutes(3))

    def producer, consumer1, consumer2, queue, environment
    def sentMessages = ["M1", "M2", "M3"]
    def consumer1Queue = new LinkedBlockingQueue()
    def consumer2Queue = new LinkedBlockingQueue()
    def common = new Common()

    def"messages were consumed at least once"() {
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

    def"messages were consumed at most once"() {
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

    def"messages were distributed to all consumers"() {
        given:
        consumer1 = new Consumer(environment, consumer1Queue::add)
        consumer2 = new Consumer(environment, consumer2Queue::add)

        when:
        consumer1.consumeMessages()
        consumer2.consumeMessages()

        then:
        def receivedMessagesConsumer1 = common.getReceivedMessages(2, Duration.ofSeconds(2), consumer1Queue)
        !receivedMessagesConsumer1.isEmpty()

        def receivedMessagesConsumer2 = common.getReceivedMessages(2, Duration.ofSeconds(2), consumer2Queue)
        !receivedMessagesConsumer2.isEmpty()
    }

    def setup() {
        environment = new EnvRabbitMQConfig(Map.of(
                PORT_VAR, String.valueOf(rabbitMQContainer.getMappedPort(5672)),
                QUEUE_NAME_VAR, "task_queue1",
                EXCHANGE_NAME_VAR, "task_exchange"))

        producer = new Producer(environment)
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

