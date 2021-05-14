package competingconsumers.version100

import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.spock.Testcontainers
import org.testcontainers.utility.DockerImageName
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CopyOnWriteArraySet

@Testcontainers
class DeliveryTest extends Specification {

    @Shared
    RabbitMQContainer rabbitMQContainer = new RabbitMQContainer(
            DockerImageName.parse("nadjahagen/rabbitmq-amqp-1_0-enabled:latest")
                    .asCompatibleSubstituteFor("rabbitmq"))
            .withExposedPorts(5672)

    def producer = new Producer(rabbitMQContainer.getMappedPort(5672))
    def allConsumedMessages = new CopyOnWriteArraySet();
    def consumer1 = new Consumer(rabbitMQContainer.getMappedPort(5672), allConsumedMessages::add)
    def consumer2 = new Consumer(rabbitMQContainer.getMappedPort(5672), allConsumedMessages::add)
    def messages = ["M1", "M2", "M3"]

    def"messages were consumed exactly once"() {
        setup:
        for(item in messages) {
            producer.sendMessage(item)
        }

        when:
        startConsumerAsynchron(consumer1)
        startConsumerAsynchron(consumer2)

        def commons = messages.intersect(allConsumedMessages) //removes eventual duplicates
        messages.removeAll(commons)
        allConsumedMessages.removeAll(commons)

        then:
        
    }

    def startConsumerAsynchron(Consumer consumer) {
        Thread t = new Thread(() -> consumer.consumeMessages())
        t.setDaemon(true)
        t.start()
    }
}
