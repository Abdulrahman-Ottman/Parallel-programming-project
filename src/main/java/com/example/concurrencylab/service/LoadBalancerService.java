package com.example.concurrencylab.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class LoadBalancerService {

    // الخوادم الخمسة (بما فيها السيرفر الذي سنعتبره معطوباً)
    private final String[] ALL_NODES = {
            "http://localhost:8000",
            "http://localhost:8001",
            "http://localhost:8002",
            "http://localhost:8003",
            "http://localhost:8004"
    };

    // مصفوفة الأوزان
    private final int[] WEIGHTS = {3, 1, 1, 2, 1};

    private int currentIndex = 0;
    private final RestTemplate restTemplate = new RestTemplate();
    private final Random random = new Random();

    // دالة لتوليد القائمة الموزونة (أو العادية) تشمل جميع الخوادم دون إقصاء مسبق
    private List<String> getRoutingList(String algorithm) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < ALL_NODES.length; i++) {
            if ("weighted".equalsIgnoreCase(algorithm)) {
                for (int w = 0; w < WEIGHTS[i]; w++) {
                    list.add(ALL_NODES[i]);
                }
            } else {
                list.add(ALL_NODES[i]); // للـ Round Robin العادي
            }
        }
        return list;
    }

    public List<String> runRealSimulation(int taskCount, String algorithm) {
        List<String> results = new ArrayList<>();
        List<String> targetList = getRoutingList(algorithm);

        for (int i = 1; i <= taskCount; i++) {
            long userId = random.nextInt(10) + 1;
            long productId = random.nextInt(5) + 1;

            boolean taskAssigned = false;
            int attempts = 0;

            // 🌟 المحاولة (Failover): سنحاول إرسال الطلب، وإذا كان السيرفر معطوباً نجرب الذي يليه
            while (!taskAssigned && attempts < targetList.size()) {
                // سحب الخادم من القائمة بناءً على المؤشر
                String targetNode = targetList.get(currentIndex % targetList.size());
                currentIndex = (currentIndex + 1) % targetList.size();
                attempts++;

                try {
                    // 1. الفحص اللحظي المباشر قبل إرسال المهمة (Health Check)
                    restTemplate.getForObject(targetNode + "/shop/health", String.class);

                    // 2. إذا نجح الفحص (لم يحدث Exception)، نرسل الطلب الحقيقي
                    String fullUrl = targetNode + "/shop/buy?userId=" + userId + "&productId=" + productId;
                    restTemplate.postForObject(fullUrl, null, String.class);

                    results.add("Task " + i + " [" + algorithm.toUpperCase() + "] [Routed to " + targetNode + "] -> Success!");
                    taskAssigned = true; // نخرج من حلقة المحاولة لأن الطلب تم بنجاح

                } catch (Exception e) {
                    // إذا كان السيرفر معطوباً، سيتم طباعة رسالة في الكونسول وتستمر الحلقة (while) للخادم التالي فوراً
                    System.out.println("⚠️ Node " + targetNode + " is down! Redirecting Task " + i + " to the next node...");
                }
            }

            // إذا انتهت كل المحاولات وكل الخوادم كانت معطوبة
            if (!taskAssigned) {
                results.add("Task " + i + " -> Failed: All nodes are completely DOWN!");
            }
        }
        return results;
    }
}