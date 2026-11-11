package com.NotificationSystem.service;

public class MessagingException extends RuntimeException {
    private final String code;

    public MessagingException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}