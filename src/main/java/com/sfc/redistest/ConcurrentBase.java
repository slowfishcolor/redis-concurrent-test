package com.sfc.redistest;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentBase {

    private static final String KEY = "count";

    public static void runTest(int totalRequest, int totalThread, final JedisPool jedisPool, Runner runner) throws InterruptedException {

        ExecutorService threadPool = Executors.newFixedThreadPool(totalThread);
        final Semaphore semaphore = new Semaphore(totalThread);
        final CountDownLatch countDownLatch = new CountDownLatch(totalRequest);

        Jedis jedis = jedisPool.getResource();
        jedis.set(KEY, "0");

        long startTime = System.currentTimeMillis();
        AtomicInteger totalExecuteCounter = new AtomicInteger(0);

        for (int i = 0; i < totalRequest; i++) {
            threadPool.execute(() -> {
                try {
                    semaphore.acquire();
                    Jedis runnerJedis = jedisPool.getResource();
                    runner.process(runnerJedis, KEY, totalExecuteCounter);
                    runnerJedis.close();
                    semaphore.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                countDownLatch.countDown();
            });
        }

        countDownLatch.await(30, TimeUnit.SECONDS);
        threadPool.shutdown();

        long endTime = System.currentTimeMillis();
        System.out.println("result: " + jedis.get(KEY) + ", execute time: " + (endTime - startTime) + "ms, " +
                "total execute: " + totalExecuteCounter + " retry: " + (totalExecuteCounter.addAndGet(-totalRequest)) );

    }

}