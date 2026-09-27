package com.NotificationSystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class AfricaTalkingSmsService {

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

        return restClient.post()
                .uri("/version1/messaging")
                .header("apiKey", apiKey)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(String.class);
    }
}