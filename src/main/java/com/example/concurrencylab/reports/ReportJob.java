package com.example.concurrencylab.reports;

import com.example.concurrencylab.model.Order;
import com.example.concurrencylab.model.User;
import com.example.concurrencylab.repository.OrderRepository;
import com.example.concurrencylab.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Service
public class ReportJob {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public ReportJob(UserRepository userRepository,
                     OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    // =========================
    // ✅ WITH BATCHING
    // =========================
    public void runReportWithBatching(ExecutorService executor) {

        List<User> users = userRepository.findAll();

        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);

        // ONE query
        List<Order> todayOrders =
                orderRepository.findByCreatedAtBetween(startOfDay, endOfDay);

        // Group in memory
        Map<Long, List<Order>> ordersByUser = new HashMap<>();
        for (Order order : todayOrders) {
            Long userId = order.getUser().getId();
            ordersByUser
                    .computeIfAbsent(userId, k -> new ArrayList<>())
                    .add(order);
        }

        // Prepare batch data
        List<UserOrderBatch> batchData = new ArrayList<>();
        for (User user : users) {
            batchData.add(new UserOrderBatch(
                    user,
                    ordersByUser.getOrDefault(user.getId(), Collections.emptyList())
            ));
        }

        // Dynamic Chunking
        int maxOrdersPerChunk = 20;

        List<List<UserOrderBatch>> chunks = new ArrayList<>();
        List<UserOrderBatch> currentChunk = new ArrayList<>();
        int currentOrdersCount = 0;

        for (UserOrderBatch batch : batchData) {
            int userOrdersCount = batch.getOrders().size();

            if (!currentChunk.isEmpty() &&
                    currentOrdersCount + userOrdersCount > maxOrdersPerChunk) {

                chunks.add(currentChunk);
                currentChunk = new ArrayList<>();
                currentOrdersCount = 0;
            }

            currentChunk.add(batch);
            currentOrdersCount += userOrdersCount;
        }

        if (!currentChunk.isEmpty()) {
            chunks.add(currentChunk);
        }

        List<Future<?>> futures = new ArrayList<>();

        for (List<UserOrderBatch> chunk : chunks) {
            futures.add(executor.submit(() -> processChunk(chunk)));
        }

        waitForFutures(futures);
    }

    // =========================
    // ❌ WITHOUT BATCHING
    // =========================
    public void runReportWithoutBatching(ExecutorService executor) {

        List<User> users = userRepository.findAll();

        LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);

        List<Future<?>> futures = new ArrayList<>();

        for (User user : users) {

            futures.add(executor.submit(() -> {

                // MANY queries (one per user)
                List<Order> orders =
                        orderRepository.findByUserIdAndCreatedAtBetween(
                                user.getId(),
                                startOfDay,
                                endOfDay
                        );

                processSingleUser(user, orders);
            }));
        }

        waitForFutures(futures);
    }

    // =========================
    // Helpers
    // =========================

    private void processChunk(List<UserOrderBatch> chunk) {
        for (UserOrderBatch item : chunk) {
            processSingleUser(item.getUser(), item.getOrders());
        }
    }

    private void processSingleUser(User user, List<Order> orders) {

        int totalOrders = orders.size();

        double totalDiscount = orders.stream()
                .mapToDouble(Order::getDiscountValue)
                .sum();
    }

    private void waitForFutures(List<Future<?>> futures) {
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private List<List<UserOrderBatch>> chunk(List<UserOrderBatch> data, int size) {
        List<List<UserOrderBatch>> chunks = new ArrayList<>();

        for (int i = 0; i < data.size(); i += size) {
            chunks.add(data.subList(i, Math.min(i + size, data.size())));
        }

        return chunks;
    }

    static class UserOrderBatch {
        private final User user;
        private final List<Order> orders;

        public UserOrderBatch(User user, List<Order> orders) {
            this.user = user;
            this.orders = orders;
        }

        public User getUser() {
            return user;
        }

        public List<Order> getOrders() {
            return orders;
        }
    }
}