package com.example.concurrencylab.service;

import com.example.concurrencylab.model.Order;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private final DistributedLockService lockService;
    private final InventoryTransactionService inventoryTxService;

    private static final String LOCK_PREFIX = "lock:inventory:";

    public InventoryService(DistributedLockService lockService,
                            InventoryTransactionService inventoryTxService) {
        this.lockService        = lockService;
        this.inventoryTxService = inventoryTxService;
    }


    public Order purchaseProduct(Long productId, int quantity, Long userId) {

        return lockService.executeWithLock(
                LOCK_PREFIX + productId,  // مثال: "lock:inventory:42"
                5,                        // انتظر 5 ثوانٍ للحصول على القفل
                10,                       // القفل يُلغى تلقائياً بعد 10 ثوانٍ
                () -> inventoryTxService.executePurchase(productId, quantity, userId)
        );
    }
}