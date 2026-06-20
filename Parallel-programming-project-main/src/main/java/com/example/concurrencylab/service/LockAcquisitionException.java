package com.example.concurrencylab.service;

/**
 * تُرمى عندما يفشل الحصول على Distributed Lock —
 * إما لأن النظام مشغول (waitTime انتهى) أو لأن الخيط انقطع.
 */
public class LockAcquisitionException extends RuntimeException {
    public LockAcquisitionException(String message) {
        super(message);
    }
}
