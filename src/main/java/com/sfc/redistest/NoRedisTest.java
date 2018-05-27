package com.sfc.redistest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

public class NoRedisTest {

    private static final int clientTotal = 5000;

    private static final int threadTotal = 50;

    private static AtomicLong count = new AtomicLong(0L);

    public static void main(String[] args) throws InterruptedException {

        ExecutorService threadPool = Executors.newFixedThreadPool(threadTotal);
        final Semaphore semaphore = new Semaphore(threadTotal);
        final CountDownLatch countDownLatch = new CountDownLatch(clientTotal);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < clientTotal; i++) {
            threadPool.execute(() -> {
                try {
                    // 保证并发数
                    semaphore.acquire();
                    add();
                    semaphore.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
            });
        }

        countDownLatch.await();
        threadPool.shutdown();

        long endTime = System.currentTimeMillis();
        System.out.println("result: " + count + ", execute time: " + (endTime - startTime) + "ms");

    }

    private static void add() {
        count.incrementAndGet();
    }
}
