package com.NotificationSystem.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
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


    public String sendTemplateMessage(String phoneNumber, String templateName, String languageCode, String collectionDate) {

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
        body.put("type", "template");

        Map<String, Object> template = new HashMap<>();
        template.put("name", templateName);

        Map<String, Object> language = new HashMap<>();
        language.put("code", languageCode);
        template.put("language", language);

        Map<String, Object> bodyComponent = new HashMap<>();
        bodyComponent.put("type", "body");

        Map<String, Object> parameter = new HashMap<>();
        parameter.put("type", "text");
        parameter.put("text", collectionDate);

        bodyComponent.put("parameters", List.of(parameter));
        template.put("components", List.of(bodyComponent));

        body.put("template", template);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, request, String.class);

            System.out.println("WhatsApp template response: " + response.getBody());
            return response.getBody();

        } catch (HttpClientErrorException e) {
            System.out.println("WhatsApp template error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return "WhatsApp API Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();
        }
    }


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

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, request, String.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            return "WhatsApp API Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();
        }
    }
}