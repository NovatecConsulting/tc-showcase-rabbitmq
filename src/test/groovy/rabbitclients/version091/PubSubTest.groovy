package rabbitclients.version091

import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.spock.Testcontainers
import rabbitclients.Common
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

    def producer, consumer1, consumer2
    def sentMessages = ["M1", "M2", "M3"]
    def consumer1Queue = new LinkedBlockingQueue()
    def consumer2Queue = new LinkedBlockingQueue()
    def common = new Common();

    def"messages were consumed by all consumers"() {
        given:
        consumer1 = new Consumer("localhost", rabbitMQContainer.getMappedPort(5672), consumer1Queue::add)
        consumer2 = new Consumer("localhost", rabbitMQContainer.getMappedPort(5672), consumer2Queue::add)
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
        producer = new Producer("localhost", rabbitMQContainer.getMappedPort(5672))
    }

    def cleanup() {
        consumer1.stop()
        consumer2.stop()
        producer.stop()
    }
}

