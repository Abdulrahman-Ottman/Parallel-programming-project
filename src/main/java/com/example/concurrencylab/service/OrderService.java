package com.example.concurrencylab.service;

import com.example.concurrencylab.GenerateInvoiceTask;
import com.example.concurrencylab.SendEmailTask;
import com.example.concurrencylab.SendNotificationTask;
import com.example.concurrencylab.aspect.TrackOrderLatency;
import com.example.concurrencylab.model.Order;
import com.example.concurrencylab.model.Product;
import com.example.concurrencylab.model.User;
import com.example.concurrencylab.repository.OrderRepository;
import com.example.concurrencylab.repository.ProductRepository;
import com.example.concurrencylab.repository.UserRepository;
import jakarta.annotation.PreDestroy;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private final ProductService productService ;

    private final DiscountService discountService = new DiscountService();
    private final BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<>();
    private final AtomicInteger asyncTasksExecuted = new AtomicInteger();
    private final ExecutorService workers = Executors.newFixedThreadPool(3);

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository,ProductService productService) {

        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productService = productService;
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
    @Transactional
    @TrackOrderLatency(scenario = "WITH_QUEUE")
    public Order buy(Long userId, Long productId, String discountCode , double discountValue) {

        boolean discountApplied = false;

        if (discountCode != null && discountCode.equals("SAVE10")) {
            discountApplied = discountService.applyDiscount(userId);
        }
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        User user = userRepository.findById(userId).orElseThrow();
        Product product = productRepository.findByIdForUpdate(productId);
        if (product.getStock() <= 0) {
            throw new RuntimeException("Out of stock");
        }

        product.setStock(product.getStock() - 1);
        productService.updateCache(product);

//        if (true) {
//            throw new RuntimeException("simulated failure after stock update");
//        }
        Order order = new Order();
        order.setUser(user);
        order.setProduct(product);
        order.setDiscountApplied(discountApplied);
        if (discountApplied){
            order.setDiscountValue(discountValue);
        }

//        pushBackgroundTasks(userId, productId);

        return orderRepository.save(order);
    }

    public Order buyNoTransaction(Long userId, Long productId, String discountCode, double discountValue) {

        boolean discountApplied = false;

        if (discountCode != null && discountCode.equals("SAVE10")) {
            discountApplied = discountService.applyDiscount(userId);
        }

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        User user = userRepository.findById(userId).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();

        if (product.getStock() <= 0) {
            throw new RuntimeException("Out of stock");
        }

        product.setStock(product.getStock() - 1);
        productRepository.save(product); // force DB write
        productService.updateCache(product);

        if (true) {
            throw new RuntimeException("simulated failure after stock update");
        }

        Order order = new Order();
        order.setUser(user);
        order.setProduct(product);
        order.setDiscountApplied(discountApplied);

        if (discountApplied) {
            order.setDiscountValue(discountValue);
        }

        return orderRepository.save(order);
    }
    private void pushBackgroundTasks(Long userId, Long productId) {


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


    @PreDestroy
    public void shutdown() {

        workers.shutdownNow();
    }
    @TrackOrderLatency(scenario = "WITHOUT_QUEUE")
    public Order buyWithoutQueue(Long userId, Long productId, String discountCode , double discountValue) {

        boolean discountApplied = false;

        if (discountCode != null && discountCode.equals("SAVE10")) {
            discountApplied = discountService.applyDiscount(userId);
        }
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        User user = userRepository.findById(userId).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();

        if (product.getStock() <= 0) {
            throw new RuntimeException("Out of stock");
        }

        product.setStock(product.getStock() - 1);

        Order order = new Order();
        order.setUser(user);
        order.setProduct(product);
        order.setDiscountApplied(discountApplied);
        if (discountApplied){
            order.setDiscountValue(discountValue);
        }
        generateInvoice(userId, productId);

        sendEmail(userId);

        sendNotification(userId);

        return orderRepository.save(order);
    }

    private void generateInvoice(Long userId, Long productId) {

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
    private void sendEmail(Long userId) {

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
    private void sendNotification(Long userId) {

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


    public int getExecutedTasks() {return asyncTasksExecuted.get();}

    public DiscountService getDiscountService() {
        return discountService;
    }
}