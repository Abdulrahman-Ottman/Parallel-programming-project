package com.example.concurrencylab.service;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class DistributedLockService {

    private final RedissonClient redissonClient;

    public DistributedLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }


    public <T> T executeWithLock(
            String lockKey,
            long waitTime,
            long leaseTime,
            Supplier<T> operation) {

        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;

        try {
            // tryLock لا يعلق إلى الأبد — ينتظر waitTime ثم يستسلم
            acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);

            if (!acquired) {
                System.out.println("[LOCK] فشل الحصول على: " + lockKey);
                throw new LockAcquisitionException(
                    "النظام مشغول، أعد المحاولة: " + lockKey);
            }

            System.out.println("[LOCK] تم الحصول على: " + lockKey);
            return operation.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockAcquisitionException("انقطع الخيط أثناء انتظار القفل");
        } finally {
            // تحرير القفل دائماً حتى لو رمت العملية exception
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                System.out.println("[LOCK] تم تحرير: " + lockKey);
            }
        }
    }
}
