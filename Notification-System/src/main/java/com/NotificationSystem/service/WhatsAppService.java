package com.NotificationSystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class WhatsAppService {

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.access-token}")
    private String accessToken;

    @Value("${whatsapp.api-version}")
    private String apiVersion;

    private final RestTemplate restTemplate = new RestTemplate();

    public String sendMessage(String phoneNumber, String message) {

        String url = "https://graph.facebook.com/"
                + apiVersion
                + "/"
                + phoneNumberId
                + "/messages";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        Map<String, Object> body = new HashMap<>();

        body.put("messaging_product", "whatsapp");
        body.put("to", phoneNumber);
        body.put("type", "text");

        Map<String, Object> text = new HashMap<>();
        text.put("body", message);

        body.put("text", text);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        try {

            ResponseEntity<String> response =
                    restTemplate.postForEntity(
                            url,
                            request,
                            String.class
                    );

            return response.getBody();

        } catch (HttpClientErrorException e) {

            return "WhatsApp API Error: "
                    + e.getStatusCode()
                    + " - "
                    + e.getResponseBodyAsString();
        }
    }
}