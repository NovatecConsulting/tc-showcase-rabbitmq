package competingconsumers.version091


import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

@Testcontainers
class DeliveryTest extends Specification {

    @Shared
    RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3")
            .withExposedPorts(5672)

    def producer = new Producer(rabbitMQContainer.getMappedPort(5672))
    def consumer1 = new Consumer(rabbitMQContainer.getMappedPort(5672))
    def consumer2 = new Consumer(rabbitMQContainer.getMappedPort(5672))
    def messages = ["M1", "M2", "M3"]

    def"messages were consumed exactly once"() {
        when:

        for(item in messages) {
            producer.sendMessage(item)
        }
        consumer1.consumeMessages()
        consumer2.consumeMessages()

        sleep(5000)

        def allConsumedMessages = consumer1.getConsumedMessages() + consumer2.getConsumedMessages()
        def commons = messages.intersect(allConsumedMessages)
        messages.removeAll(commons)
        allConsumedMessages.removeAll(commons)

        then:
        messages.isEmpty()
        allConsumedMessages.isEmpty()
    }
}
