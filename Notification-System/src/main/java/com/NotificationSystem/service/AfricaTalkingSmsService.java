package com.NotificationSystem.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AfricaTalkingSmsService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final RestClient restClient;
    private final String username;
    private final String apiKey;
    private final String baseUrl;

    public AfricaTalkingSmsService(
            @Value("${africastalking.username}") String username,
            @Value("${africastalking.api-key}") String apiKey,
            @Value("${africastalking.base-url:https://api.africastalking.com}") String baseUrl
    ) {
        this.username = username;
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    // Shows once at startup which account the app talks to (never logs the key)
    @PostConstruct
    void logConfig() {
        log.info("Africa's Talking config: baseUrl={}, username={}, apiKeySet={}",
                baseUrl, username, apiKey != null && !apiKey.isBlank());
    }

    public String sendSms(String phoneNumber, String message) {
        String masked = mask(phoneNumber);
        log.info("AT SMS: sending to {} ({} chars)", masked, message == null ? 0 : message.length());

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
            log.warn("AT SMS: HTTP {} for {}: {}", http, masked, e.getResponseBodyAsString());
            throw new MessagingException("HTTP_" + http, "Africa's Talking returned HTTP " + http, e);
        } catch (ResourceAccessException e) {
            log.warn("AT SMS: could not reach {} for {}: {}", baseUrl, masked, e.getMessage());
            throw new MessagingException("NETWORK", "Could not reach Africa's Talking: " + e.getMessage(), e);
        }

        log.debug("AT SMS: raw response for {}: {}", masked, json);
        assertAccepted(json, masked);
        return json;
    }

    private void assertAccepted(String json, String masked) {
        JsonNode data;
        try {
            data = MAPPER.readTree(json).path("SMSMessageData");
        } catch (Exception e) {
            log.warn("AT SMS: unreadable response for {}: {}", masked, json);
            return;
        }

        JsonNode r = data.path("Recipients").path(0);
        if (r.isMissingNode()) {
            log.warn("AT SMS: no recipient accepted for {}: {}", masked, data.path("Message").asText(""));
            throw new MessagingException("NO_RECIPIENT",
                    "Africa's Talking accepted no recipient: " + data.path("Message").asText(""), null);
        }

        int statusCode = r.path("statusCode").asInt(0);
        String status = r.path("status").asText("Unknown");

        if (statusCode < 100 || statusCode > 102) {   // 100 processed, 101 sent, 102 queued
            log.warn("AT SMS: rejected for {}: status={} statusCode={}", masked, status, statusCode);
            throw new MessagingException(status, "Africa's Talking rejected the message: " + status, null);
        }

        log.info("AT SMS: accepted for {}: status={} statusCode={} messageId={} cost={}",
                masked, status, statusCode,
                r.path("messageId").asText(""), r.path("cost").asText(""));
    }

    // +254712345678 -> +254****5678
    private static String mask(String phone) {
        if (phone == null || phone.length() < 8) return "****";
        return phone.substring(0, 4) + "****" + phone.substring(phone.length() - 4);
    }
}