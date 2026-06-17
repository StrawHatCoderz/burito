package com.burito.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentService {
    
    /**
     * Stub implementation that always succeeds.
     */
    public boolean processPayment(Double amount) {
        log.info("Processing mock payment for amount: ${}", amount);
        return true;
    }
}
