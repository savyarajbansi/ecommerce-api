package com.ecommerce.api.service;

import com.ecommerce.api.exception.BadRequestException;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.param.ChargeCreateParams;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class PaymentService {

    public String createCharge(String token, BigDecimal amount, String description) {
        try {
            ChargeCreateParams params = ChargeCreateParams.builder()
                    .setAmount(convertToStripeAmount(amount))
                    .setCurrency("usd")
                    .setSource(token)
                    .setDescription(description)
                    .build();
            Charge charge = Charge.create(params);
            return charge.getId();
        } catch (StripeException e) {
            throw new BadRequestException("Payment failed: " + e.getMessage());
        }
    }

    public long convertToStripeAmount(BigDecimal dollars) {
        if (dollars.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Amount must be positive");
        }
        return dollars.multiply(new BigDecimal("100")).longValue();
    }
}
