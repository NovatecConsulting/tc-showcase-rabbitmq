package rabbitclients.version100.publishsubscribe;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class Setup {
    private final HttpClient httpClient;
    private final int port;
    private HttpPut httpPut;
    private HttpPost httpPost;

    public Setup(int port) {
        httpClient = HttpClientBuilder.create().build();
        this.port = port;
    }

    public void createExchange(String exchangeName) throws IOException {
        httpPut = new HttpPut("http://localhost:" + port + "/api/exchanges/%2f/" + exchangeName);
        createHeader(httpPut);

        StringEntity params = new StringEntity("{\"type\":\"fanout\",\"auto_delete\":false,\"durable\":false,\"internal\":false,\"arguments\":{}}");
        httpPut.setEntity(params);

        httpClient.execute(httpPut);
    }

    public String createQueueWithRandomName() throws IOException {
        String queueName = generateRandomQueueName(16);
        httpPut = new HttpPut("http://localhost:" + port + "/api/queues/%2f/" + queueName);
        createHeader(httpPut);

        StringEntity params = new StringEntity("{\"auto_delete\":false,\"durable\":false,\"arguments\":{}}");
        httpPut.setEntity(params);

        httpClient.execute(httpPut);
        return queueName;
    }

    public void createBinding(String exchangeName, String queueName) throws IOException {
        httpPost = new HttpPost("http://localhost:" + port + "/api/bindings/%2f/e/" + exchangeName + "/q/" + queueName);
        createHeader(httpPost);

        StringEntity params = new StringEntity("{\"routing_key\":\"" + queueName + "\",\"arguments\":{}}");
        httpPost.setEntity(params);

        httpClient.execute(httpPost);
    }

    private void createHeader(HttpRequest httpRequest) {
        httpRequest.addHeader("content-type", "application/json");

        String auth = "guest:guest";
        byte[] encodedAuth = Base64.encodeBase64(
                auth.getBytes(StandardCharsets.ISO_8859_1));
        String authHeader = "Basic " + new String(encodedAuth);
        httpRequest.addHeader(HttpHeaders.AUTHORIZATION, authHeader);
    }

    public static String generateRandomQueueName(int length) {
        Random rng = new Random();
        String characters = "abcdefghijklmnopqrstuvwxyz1234567890";

        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }
}
