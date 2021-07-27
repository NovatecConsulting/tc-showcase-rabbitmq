package rabbitclients.version091

import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.spock.Testcontainers
import rabbitclients.Common
import rabbitclients.EnvRabbitMQConfig
import rabbitclients.version091.publishsubscribe.Consumer
import rabbitclients.version091.publishsubscribe.Producer
import spock.lang.Shared
import spock.lang.Specification
import java.time.Duration
import java.util.concurrent.LinkedBlockingQueue

@Testcontainers
class PubSubTest extends Specification {

    @Shared
    RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3")
            .withExposedPorts(5672)
            .withStartupTimeout(Duration.ofMinutes(3))

    def producer, consumer1, consumer2, environment1, environment2, environment3
    def sentMessages = ["M1", "M2", "M3"]
    def consumer1Queue = new LinkedBlockingQueue()
    def consumer2Queue = new LinkedBlockingQueue()
    def common = new Common()

    def"messages were consumed by all consumers"() {
        given:
        consumer1 = new Consumer(environment1, consumer1Queue::add)
        consumer2 = new Consumer(environment2, consumer2Queue::add)
        for(item in sentMessages) {
            producer.sendMessage(item)
        }

        when:
        consumer1.consumeMessages()
        consumer2.consumeMessages()

        then:
        def receivedMessages1 = common.getReceivedMessages(3, Duration.ofSeconds(2), consumer1Queue)
        sentMessages.size() <= receivedMessages1.size()
        receivedMessages1.containsAll(sentMessages)

        def receivedMessages2 = common.getReceivedMessages(3, Duration.ofSeconds(2), consumer2Queue)
        sentMessages.size() <= receivedMessages2.size()
        receivedMessages2.containsAll(sentMessages)
    }

    def setup() {
        def envMap1 = new Properties()
        envMap1.put("PORT", String.valueOf(rabbitMQContainer.getMappedPort(5672)))
        envMap1.put("QUEUE_NAME", "task_queue1")
        envMap1.put("EXCHANGE_NAME", "task_exchange")
        environment1 = new EnvRabbitMQConfig(envMap1 as Map<String, String>)

        def envMap2 = new Properties()
        envMap2.put("PORT", String.valueOf(rabbitMQContainer.getMappedPort(5672)))
        envMap2.put("QUEUE_NAME", "task_queue2")
        envMap2.put("EXCHANGE_NAME", "task_exchange")
        environment2 = new EnvRabbitMQConfig(envMap2 as Map<String, String>)

        def envMap3 = new Properties();
        envMap3.put("PORT", String.valueOf(rabbitMQContainer.getMappedPort(5672)))
        envMap3.put("QUEUE_NAME", "task_queue3")
        envMap3.put("EXCHANGE_NAME", "task_exchange")
        environment3 = new EnvRabbitMQConfig(envMap3 as Map<String, String>)

        producer = new Producer(environment3)
    }

    def cleanup() {
        consumer1.stop()
        consumer2.stop()
        producer.stop()
    }
}

