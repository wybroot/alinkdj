package com.honghe.party.notice;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

/** No automatic transport retries: a timed out send may already have been accepted. */
@Component
public class ProviderHttpClient {
    private final RestClient client;

    public ProviderHttpClient() {
        var factory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5)).followRedirects(HttpClient.Redirect.NEVER).build());
        factory.setReadTimeout(Duration.ofSeconds(15));
        client = RestClient.builder().requestFactory(factory).build();
    }

    public ProviderHttpClient(RestClient client) { this.client = client; }

    public JsonNode get(URI uri) {
        return client.get().uri(uri).retrieve().body(JsonNode.class);
    }

    public JsonNode post(URI uri, Object body) {
        return client.post().uri(uri).contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(JsonNode.class);
    }
}
