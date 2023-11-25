package com.sms.twilio.service;

import com.sms.twilio.config.TwilioConfig;
import com.sms.twilio.dto.OtpStatus;
import com.sms.twilio.dto.PasswordResetRequestDto;
import com.sms.twilio.dto.PasswordResetResponseDto;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class TwilioOTPService {

    @Autowired
    TwilioConfig twilioConfig;

    // TODO: 23.11.2023 Need use bd
    Map<String,String> otpMap = new HashMap<>();

    public Mono<PasswordResetResponseDto> sendOTPForPasswordReset(PasswordResetRequestDto passwordResetRequestDto) {

        PasswordResetResponseDto passwordResetResponseDto = null;

        try {
            PhoneNumber to = new PhoneNumber(passwordResetRequestDto.getPhoneNumber());
            PhoneNumber from = new PhoneNumber(twilioConfig.getTrialNumber());
            String otp = generateOTP();
            String otpMassage = ("your code " + otp);

            Message message = Message.creator(to, from, otpMassage).create();

            otpMap.put(passwordResetRequestDto.getUserName(),otp);

            passwordResetResponseDto = new PasswordResetResponseDto(OtpStatus.DELIVERED, otpMassage);
        } catch (Exception ex) {
            passwordResetResponseDto = new PasswordResetResponseDto(OtpStatus.FAILED, ex.getMessage());
        }
        return Mono.just(passwordResetResponseDto);
    }

    public Mono<String> validateOTP(String userInputOtp, String userName) {
        if (userInputOtp.equals(otpMap.get(userName))) {
            // TODO: 24.11.2023 Save user in DB!!
            otpMap.remove(userName, userInputOtp);
            return Mono.just("Valid OTP please proceed with your transaction !");
        } else {
            return Mono.error(new IllegalArgumentException("Invalid otp please retry !"));
        }
    }

    //6 digit otp
    private String generateOTP() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
