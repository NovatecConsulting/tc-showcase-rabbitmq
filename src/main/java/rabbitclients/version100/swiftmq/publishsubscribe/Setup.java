package rabbitclients.version100.swiftmq.publishsubscribe;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import rabbitclients.RabbitMQConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Setup {
    private final HttpClient httpClient;
    private final RabbitMQConfig rabbitMQConfig;

    public Setup(RabbitMQConfig rabbitMQConfig) {
        httpClient = HttpClientBuilder.create().build();
        this.rabbitMQConfig = rabbitMQConfig;
    }

    /**
     * Creates a new fanout exchange with default-configurations: no auto-deletion, not durable
     * @param exchangeName name of the new fanout exchange
     * @throws IOException
     */
    public void createExchange(String exchangeName) throws IOException {
        HttpPut httpPut = new HttpPut(buildUri("/api/exchanges/%2f/" + exchangeName));

        StringEntity params = new StringEntity("{\"type\":\"fanout\",\"auto_delete\":false,\"durable\":false,\"internal\":false,\"arguments\":{}}");
        httpPut.setEntity(params);
        send(httpPut);
    }

    /**
     * Creates a new queue with default-configurations: no auto-deletion, not durable.
     * @param queueName name of the new queue
     * @throws IOException
     */
    public void createQueue(String queueName) throws IOException {
        HttpPut httpPut = new HttpPut(buildUri("/api/queues/%2f/" + queueName));

        StringEntity params = new StringEntity("{\"auto_delete\":false,\"durable\":false,\"arguments\":{}}");
        httpPut.setEntity(params);
        send(httpPut);
    }

    /**
     * Creates a new binding between the queue and the exchange. The queue's name will be used as routing key.
     * @param queueName name of the queue to bind
     * @param exchangeName name of the exchange to bind
     * @throws IOException
     */
    public void createBinding(String queueName, String exchangeName) throws IOException {
        HttpPost httpPost = new HttpPost(buildUri("/api/bindings/%2f/e/" + exchangeName + "/q/" + queueName));

        StringEntity params = new StringEntity("{\"routing_key\":\"" + queueName + "\",\"arguments\":{}}");
        httpPost.setEntity(params);
        send(httpPost);
    }

    /**
     * Creates a new Http Header for authentication.
     * @param httpRequest
     */
    private void createHeader(HttpRequest httpRequest) {
        httpRequest.addHeader("content-type", "application/json");

        String auth = rabbitMQConfig.getUser() + ":" + rabbitMQConfig.getPassword();
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(StandardCharsets.ISO_8859_1));
        String authHeader = "Basic " + new String(encodedAuth);
        httpRequest.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
    }

    private String buildUri(String uri) {
        String uriBeginning = "http://" + rabbitMQConfig.getHost() + ":" + rabbitMQConfig.getManagementPort();
        return uriBeginning + uri;
    }

    private void send(HttpUriRequest httpRequest) throws IOException {
        createHeader(httpRequest);
        httpClient.execute(httpRequest);
    }
}
