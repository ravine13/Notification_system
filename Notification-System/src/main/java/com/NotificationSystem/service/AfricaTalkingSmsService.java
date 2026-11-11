package com.NotificationSystem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class AfricaTalkingSmsService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final RestClient restClient;
    private final String username;
    private final String apiKey;

    public AfricaTalkingSmsService(
            @Value("${africastalking.username}") String username,
            @Value("${africastalking.api-key}") String apiKey,
            @Value("${africastalking.base-url:https://api.africastalking.com}") String baseUrl
    ) {
        this.username = username;
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public String sendSms(String phoneNumber, String message) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("username", username);
        form.add("to", phoneNumber);
        form.add("message", message);

        String json;
        try {
            json = restClient.post()
                    .uri("/version1/messaging")
                    .header("apiKey", apiKey)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientResponseException e) {
            int http = e.getStatusCode().value();
            throw new MessagingException("HTTP_" + http, "Africa's Talking returned HTTP " + http, e);
        } catch (ResourceAccessException e) {
            throw new MessagingException("NETWORK", "Could not reach Africa's Talking: " + e.getMessage(), e);
        }

        assertAccepted(json);
        return json;
    }

    private void assertAccepted(String json) {
        JsonNode data;
        try {
            data = MAPPER.readTree(json).path("SMSMessageData");
        } catch (Exception e) {
            return; // unreadable body: keep the old behaviour
        }

        JsonNode r = data.path("Recipients").path(0);
        if (r.isMissingNode()) {
            throw new MessagingException("NO_RECIPIENT",
                    "Africa's Talking accepted no recipient: " + data.path("Message").asText(""), null);
        }

        int statusCode = r.path("statusCode").asInt(0);
        if (statusCode < 100 || statusCode > 102) {   // 100 processed, 101 sent, 102 queued
            String status = r.path("status").asText("Unknown");
            throw new MessagingException(status, "Africa's Talking rejected the message: " + status, null);
        }
    }
}