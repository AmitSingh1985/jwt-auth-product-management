package com.amit.auth.dto;

public class OtpResponse {

    private String message;
    private String otp;

    public OtpResponse(String message, String otp) {
        this.message = message;
        this.otp = otp;
    }

    public String getMessage() {
        return message;
    }

    public String getOtp() {
        return otp;
    }
}

