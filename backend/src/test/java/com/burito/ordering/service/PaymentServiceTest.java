package com.burito.ordering.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentServiceTest {

    @Test
    void processPayment_alwaysReturnsTrue() {
        PaymentService paymentService = new PaymentService();
        boolean result = paymentService.processPayment(100.0);
        assertTrue(result);
    }
}
