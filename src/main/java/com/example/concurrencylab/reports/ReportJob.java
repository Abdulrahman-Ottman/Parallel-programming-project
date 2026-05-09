package com.example.concurrencylab.reports;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ReportJob {
    @Async
    @Scheduled(fixedRate = 10000)
    public void runReport() {

        System.out.println("📊 Report started: " + Thread.currentThread().getName());

        try {
            Thread.sleep(5000); // simulate heavy work
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("✅ Report finished");
    }
}
