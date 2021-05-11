package competingconsumers.version091


import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.spock.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.LinkedBlockingQueue

@Testcontainers
class DeliveryTest extends Specification {

    @Shared
    RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:3")
            .withExposedPorts(5672)

    def producer = new Producer(rabbitMQContainer.getMappedPort(5672))
    def queue = new LinkedBlockingQueue();
    def allConsumedMessages = new CopyOnWriteArraySet();
    def consumer1 = new Consumer(rabbitMQContainer.getMappedPort(5672), queue::add)
    def consumer2 = new Consumer(rabbitMQContainer.getMappedPort(5672), queue::add)
    def messages = ["M1", "M2", "M3"]

    def"messages were consumed exactly once"() {
        setup:
        for(item in messages) {
            producer.sendMessage(item)
        }

        when:
        consumer1.consumeMessages()
        consumer2.consumeMessages()

        waitForThreeMessages() //make sure that enough time has passed to successfully consume three messages

        def commons = messages.intersect(allConsumedMessages) //removes eventual duplicates
        messages.removeAll(commons)
        allConsumedMessages.removeAll(commons)

        then:
        messages.isEmpty() //were all messages consumed that have been sent?
        allConsumedMessages.isEmpty() //were there any duplicates in sent messages?
        queue.isEmpty() //did more messages arrive than were sent?
    }

    def waitForThreeMessages() {
        for(int i = 0; i < 3; i++) {
            allConsumedMessages.add(queue.take()) //will be called back as soon as an object is available
        }
    }
}
