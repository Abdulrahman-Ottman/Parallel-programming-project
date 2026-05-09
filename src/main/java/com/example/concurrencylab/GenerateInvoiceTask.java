package com.example.concurrencylab;

public class GenerateInvoiceTask implements Runnable {

    private final String userId;
    private final String productId;

    public GenerateInvoiceTask(
            String userId,
            String productId
    ) {
        this.userId = userId;
        this.productId = productId;
    }

    @Override
    public void run() {

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
}
