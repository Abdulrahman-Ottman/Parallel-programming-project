package com.example.concurrencylab.service;

import com.example.concurrencylab.GenerateInvoiceTask;
import com.example.concurrencylab.SendEmailTask;
import com.example.concurrencylab.SendNotificationTask;
import com.example.concurrencylab.aspect.TrackOrderLatency;
import com.example.concurrencylab.model.Order;
import com.example.concurrencylab.repository.OrderRepository;
import com.example.concurrencylab.repository.ProductRepository;
import com.example.concurrencylab.repository.UserRepository;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    /**
     * المطلب السابع — التعديل الجوهري:
     * أضفنا InventoryService حتى كل عملية شراء تمر عبر
     * Distributed Lock بدل تعديل المخزون مباشرة.
     *
     * الكود القديم (Race Condition):
     *   product.setStock(product.getStock() - 1);  ← خطأ
     *
     * الكود الجديد (محمي):
     *   inventoryService.purchaseProduct(...)       ← صحيح
     */
    private final InventoryService inventoryService;

    private final DiscountService discountService = new DiscountService();
    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private final AtomicInteger asyncTasksExecuted = new AtomicInteger();
    private final ExecutorService workers = Executors.newFixedThreadPool(3);

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository,
                        InventoryService inventoryService) {
        this.orderRepository   = orderRepository;
        this.userRepository    = userRepository;
        this.productRepository = productRepository;
        this.inventoryService  = inventoryService;

        // تشغيل Workers بالخلفية — المطلب الثالث (Async Queues)
        for (int i = 1; i <= 3; i++) {
            int workerId = i;
            workers.submit(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Runnable task = taskQueue.take();
                        System.out.println("[Worker-" + workerId + "] picked task");
                        task.run();
                        asyncTasksExecuted.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
    }

    @TrackOrderLatency(scenario = "WITH_QUEUE")
    public Order buy(Long userId, Long productId, String discountCode, double discountValue) {

        // ── 1. تطبيق الخصم إن وُجد ──────────────────────────────
        boolean discountApplied = false;
        if (discountCode != null && discountCode.equals("SAVE10")) {
            discountApplied = discountService.applyDiscount(userId);
        }

        // ── 2. محاكاة معالجة (50ms) ──────────────────────────────
        try { Thread.sleep(50); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // ── 3. الشراء عبر InventoryService (Distributed Lock) ────
        Order order = inventoryService.purchaseProduct(productId, 1, userId);

        // ── 4. تطبيق بيانات الخصم على الطلب ─────────────────────
        order.setDiscountApplied(discountApplied);
        if (discountApplied) {
            order.setDiscountValue(discountValue);
        }
        orderRepository.save(order);

        // ── 5. دفع المهام الخلفية للـ Queue ──────────────────────
        pushBackgroundTasks(userId, productId);

        return order;
    }

    @TrackOrderLatency(scenario = "WITHOUT_QUEUE")
    public Order buyWithoutQueue(Long userId, Long productId, String discountCode, double discountValue) {

        boolean discountApplied = false;
        if (discountCode != null && discountCode.equals("SAVE10")) {
            discountApplied = discountService.applyDiscount(userId);
        }

        try { Thread.sleep(50); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // نفس الحماية عبر InventoryService
        Order order = inventoryService.purchaseProduct(productId, 1, userId);

        order.setDiscountApplied(discountApplied);
        if (discountApplied) {
            order.setDiscountValue(discountValue);
        }
        orderRepository.save(order);

        // المهام تُنفَّذ بالتسلسل (بدون queue) — للمقارنة في Benchmark
        generateInvoice(userId, productId);
        sendEmail(userId);
        sendNotification(userId);

        return order;
    }

    private void pushBackgroundTasks(Long userId, Long productId) {
        try {
            taskQueue.put(new GenerateInvoiceTask(userId, productId));
            taskQueue.put(new SendEmailTask(userId));
            taskQueue.put(new SendNotificationTask(userId));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void generateInvoice(Long userId, Long productId) {
        try {
            System.out.println("Generating invoice for " + userId);
            Thread.sleep(1200);
            System.out.println("Invoice generated for " + userId);
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private void sendEmail(Long userId) {
        try {
            System.out.println("Sending email to " + userId);
            Thread.sleep(2000);
            System.out.println("Email sent to " + userId);
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    private void sendNotification(Long userId) {
        try {
            System.out.println("Sending notification to " + userId);
            Thread.sleep(800);
            System.out.println("Notification sent to " + userId);
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    @PreDestroy
    public void shutdown() { workers.shutdownNow(); }

    public int getExecutedTasks()              { return asyncTasksExecuted.get(); }
    public DiscountService getDiscountService() { return discountService; }
}
