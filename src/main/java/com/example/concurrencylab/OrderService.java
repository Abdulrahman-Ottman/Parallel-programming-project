package com.example.concurrencylab;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.annotation.PreDestroy;

public class OrderService {

//  private List<String> orders = new ArrayList<>();
    private final List<String> orders =
            new CopyOnWriteArrayList<>();

    private DiscountService discountService = new DiscountService();

    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private final AtomicInteger asyncTasksExecuted = new AtomicInteger();
    private final ExecutorService workers = Executors.newFixedThreadPool(3);


    public OrderService() {

        // تشغيل Workers بالخلفية

        for (int i = 1; i <= 3; i++) {

            int workerId = i;

            workers.submit(() -> {

                while (!Thread.currentThread().isInterrupted()) {

                    try {

                        Runnable task = taskQueue.take();

                        System.out.println(
                                "[Worker-" + workerId + "] picked task"
                        );

                        task.run();

                        asyncTasksExecuted.incrementAndGet();

                    } catch (InterruptedException e) {

                        Thread.currentThread().interrupt();
                    }
                }
            });
        }
    }
    public String buy(String userId, String productId, String discountCode) {

        boolean discountApplied = false;

        if (discountCode != null && discountCode.equals("SAVE10")) {
            discountApplied = discountService.applyDiscount(userId);
        }
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        simulateDatabaseOperation();



        String order = "User: " + userId +
                ", Product: " + productId +
                ", Discount: " + discountApplied;

        orders.add(order);

        pushBackgroundTasks(userId, productId);

        return order;
    }

    private void pushBackgroundTasks(String userId, String productId) {


        try {

            taskQueue.put(
                    new GenerateInvoiceTask(userId, productId)
            );

            taskQueue.put(
                    new SendEmailTask(userId)
            );

            taskQueue.put(
                    new SendNotificationTask(userId)
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }
    }
    private void simulateDatabaseOperation() {

        try {

            Thread.sleep(50);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }
    }
    @PreDestroy
    public void shutdown() {

        workers.shutdownNow();
    }

    public String buyWithoutQueue(String userId, String productId, String discountCode) {
        boolean discountApplied = false;
        if (discountCode != null &&
                discountCode.equals("SAVE10")) {
            discountApplied =
                    discountService.applyDiscount(userId);
        }
        simulateDatabaseOperation();
        String order =
                "User: " + userId +
                        ", Product: " + productId +
                        ", Discount: " + discountApplied;

        orders.add(order);

        generateInvoice(userId, productId);

        sendEmail(userId);

        sendNotification(userId);

        return order;
    }
    private void generateInvoice(String userId, String productId) {

        try {

            System.out.println(
                    "Generating invoice for " + userId
            );

            Thread.sleep(1200);

            System.out.println(
                    "Invoice generated for " + userId
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }
    }
    public List<String> getOrders() {return orders;}
    private void sendEmail(String userId) {

        try {

            System.out.println(
                    "Sending email to " + userId
            );

            Thread.sleep(2000);

            System.out.println(
                    "Email sent to " + userId
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }
    }
    private void sendNotification(String userId) {

        try {

            System.out.println(
                    "Sending notification to " + userId
            );

            Thread.sleep(800);

            System.out.println(
                    "Notification sent to " + userId
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }
    }
    public DiscountService getDiscountService() {
        return discountService;
    }
    public int getExecutedTasks() {return asyncTasksExecuted.get();
    }
}