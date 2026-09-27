package com.NotificationSystem.controller;

import com.NotificationSystem.service.AfricaTalkingSmsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sms")
public class SmsController {

    private final AfricaTalkingSmsService smsService;

    public SmsController(AfricaTalkingSmsService smsService) {
        this.smsService = smsService;
    }

    @PostMapping("/send")
    public String sendSms(
            @RequestParam String phone,
            @RequestParam String message
    ) {
        System.out.println("Sending sms to ravine");
        smsService.sendSms(phone, message);

        return "SMS request sent";
    }
}