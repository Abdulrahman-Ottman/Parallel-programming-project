package com.example.concurrencylab;

public class SendEmailTask implements Runnable {

    private final String userId;

    public SendEmailTask(String userId) {
        this.userId = userId;
    }

    @Override
    public void run() {

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
}