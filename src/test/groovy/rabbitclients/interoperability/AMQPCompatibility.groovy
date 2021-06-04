package rabbitclients.interoperability

import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.spock.Testcontainers
import rabbitclients.Common
import rabbitclients.version091.competingconsumers.Producer
import rabbitclients.version100.competingconsumers.Consumer
import spock.lang.Shared
import spock.lang.Specification
import java.time.Duration
import java.util.concurrent.LinkedBlockingQueue

/**
 * Test of compatibility/interoperability of AMQP 1.0 and AMQP 0.9.1.
 * In general, compatibility can only be ensured for 0.9.1-Producers and 1.0-Consumers and not vice versa.
 * More details on this are provided in the README-file of this project.
 */
@Testcontainers
class AMQPCompatibility extends Specification {

    @Shared
    RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3")
            .withPluginsEnabled("rabbitmq_amqp1_0")
            .withExposedPorts(5672)

    def producer, consumer1, queue
    def sentMessages = ["M1", "M2", "M3"]
    def common = new Common()

    def "consumed messages equal sent messages"() {
        given:
        queue = new LinkedBlockingQueue()
        consumer1 = new Consumer("localhost", rabbitMQContainer.getMappedPort(5672), queue::add)

        when:
        common.startConsumerAsynchron(consumer1)

        then:
        def receivedMessages = common.getReceivedMessages(3, Duration.ofSeconds(2), queue)
        sentMessages.size() >= receivedMessages.size()
        receivedMessages.containsAll(sentMessages)
    }

    def setup() {
        producer = new Producer("localhost", rabbitMQContainer.getMappedPort(5672))
        for (item in sentMessages) {
            producer.sendMessage(item)
        }
    }

    def cleanup() {
        consumer1.stop()
        producer.stop()
    }
}
