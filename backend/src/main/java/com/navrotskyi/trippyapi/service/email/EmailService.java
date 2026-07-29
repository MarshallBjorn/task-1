package com.navrotskyi.trippyapi.service.email;

public interface EmailService {
    void sendVerificationEmail(String to, String token);
}
