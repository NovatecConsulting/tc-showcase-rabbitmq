package rabbitclients.version100;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import rabbitclients.RabbitMQConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Setup class that is accessing the REST-API of RabbitMQ's Management plugin (needs to be enabled!).
 * Since AMQP 1.0 does not include concepts like queues and exchanges, the Java clients to not offer to create those
 * entities programmatically. Therefore, the REST-API has to be used to create entities at runtime.
 * The Management plugin is running at default-port 15672. For shovels, the Shovel plugin needs to be enabled as well.
 */
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
     * Creates a shovel which transfers messages from the source queue/exchange/address to the destination
     * queue/exchange/address. For AMQP 0.9.1 endpoints, the queue or exchange needs to be specified.
     * For AMQP 1.0 endpoints, the address needs to be specified. More details are available at the
     * Shovel-plugin documentation and the AMQP-1.0-plugin documentation at Github.
     * @param srcProtocol either "amqp091" or "amqp10"
     * @param destProtocol either "amqp091" or "amqp10"
     * @param srcAddress queue/exchange/address
     * @param destAddress queue/exchange/address
     * @throws IOException
     */
    public void createShovelConfig(String srcProtocol, String destProtocol, String srcAddress, String destAddress)
            throws IOException {
        HttpPut httpPut = new HttpPut(buildUri("/api/parameters/shovel/%2f/shovel"));

        //build shovel configuration in JSON format
        JSONObject val = new JSONObject();

        //Shovel source: AMQP 0.9.1 = queue/exchange; AMQP 1.0 = address endpoint
        val.put("src-protocol", srcProtocol);
        val.put("src-uri", buildShovelUri(""));
        if(srcProtocol.equals("amqp091")) {
            val.put("src-queue", srcAddress);
        }else {
            val.put("src-address", srcAddress);
        }

        //Shovel destination: AMQP 0.9.1 = queue/exchange; AMQP 1.0 = address endpoint
        val.put("dest-protocol", destProtocol);
        val.put("dest-uri", buildShovelUri(""));
        if(destProtocol.equals("amqp091")) {
            val.put("dest-queue", destAddress);
        }else {
            val.put("dest-address", destAddress);
        }

        //wrap with value block
        JSONObject json = new JSONObject();
        json.put("value", val);

        StringEntity params = new StringEntity(json.toString());
        httpPut.setEntity(params);
        send(httpPut);
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

    private String buildShovelUri(String uri) {
        String uriBeginning = "amqp://" + rabbitMQConfig.getHost() + ":" + rabbitMQConfig.getPort();
        return uriBeginning + uri;
    }

    private void send(HttpUriRequest httpRequest) throws IOException {
        createHeader(httpRequest);
        HttpResponse h = httpClient.execute(httpRequest);
        System.out.println(h);
    }
}
