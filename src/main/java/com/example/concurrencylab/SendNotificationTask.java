package com.example.concurrencylab;

public class SendNotificationTask implements Runnable {

    private final Long userId;

    public SendNotificationTask(Long userId) {
        this.userId = userId;
    }

    @Override
    public void run() {

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
}