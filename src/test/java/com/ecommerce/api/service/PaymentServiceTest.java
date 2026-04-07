package com.ecommerce.api.service;

import com.ecommerce.api.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService();
    }

    @Test
    void convertToStripeAmount_dollars_convertsToCents() {
        assertEquals(999L, paymentService.convertToStripeAmount(new BigDecimal("9.99")));
        assertEquals(1000L, paymentService.convertToStripeAmount(new BigDecimal("10.00")));
        assertEquals(100L, paymentService.convertToStripeAmount(new BigDecimal("1.00")));
    }

    @Test
    void convertToStripeAmount_zero_throwsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> paymentService.convertToStripeAmount(BigDecimal.ZERO));
    }

    @Test
    void convertToStripeAmount_negative_throwsBadRequest() {
        assertThrows(BadRequestException.class,
                () -> paymentService.convertToStripeAmount(new BigDecimal("-5.00")));
    }
}
