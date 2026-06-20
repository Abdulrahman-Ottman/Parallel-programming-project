package com.example.concurrencylab.service;

import com.example.concurrencylab.model.Order;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private final DistributedLockService lockService;
    private final InventoryTransactionService inventoryTxService;

    // صيغة المفتاح: قفل خاص بكل منتج → لا تعارض بين منتجات مختلفة
    private static final String LOCK_PREFIX = "lock:inventory:";

    public InventoryService(DistributedLockService lockService,
                            InventoryTransactionService inventoryTxService) {
        this.lockService        = lockService;
        this.inventoryTxService = inventoryTxService;
    }

    /**
     * المطلب السابع — Concurrency Control (Pessimistic Distributed Lock):
     *
     * تعديل المخزون محمي بـ Distributed Lock عبر Redis.
     * كل منتج له قفله الخاص (lock:inventory:{productId}) —
     * بمعنى طلبان على منتجين مختلفين لا يتعارضان.
     *
     * الضمانات التي يقدمها هذا الأسلوب:
     *   1. خيط واحد فقط يعدّل مخزون منتج معين في أي لحظة
     *   2. لا Race Condition → لا Overselling
     *   3. leaseTime يمنع Dead Lock إذا مات الخيط فجأة
     *   4. tryLock بدل lock() → لا انتظار أبدي
     *
     * ملاحظة: لا @Transactional هنا عن قصد —
     * الـ Transaction تبدأ داخل executeWithLock بعد أخذ اللوك،
     * حتى يشوف كل خيط الـ DB state المحدّثة من الخيط اللي قبله.
     */
    public Order purchaseProduct(Long productId, int quantity, Long userId) {

        return lockService.executeWithLock(
                LOCK_PREFIX + productId,  // مثال: "lock:inventory:42"
                5,                        // انتظر 5 ثوانٍ للحصول على القفل
                10,                       // القفل يُلغى تلقائياً بعد 10 ثوانٍ
                () -> inventoryTxService.executePurchase(productId, quantity, userId)
        );
    }
}