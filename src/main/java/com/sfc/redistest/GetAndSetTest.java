package com.sfc.redistest;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class GetAndSetTest {

    private static final int clientTotal = 10000;

    private static final int threadTotal = 50;

    private static final String KEY = "count";

    public static void main(String[] args) throws InterruptedException {

        ExecutorService threadPool = Executors.newFixedThreadPool(threadTotal);
        final Semaphore semaphore = new Semaphore(threadTotal);
        final CountDownLatch countDownLatch = new CountDownLatch(clientTotal);

        JedisPool jedisPool = new JedisPool("localhost");
        Jedis mainJedis = jedisPool.getResource();
        mainJedis.set(KEY, "0");

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < clientTotal; i++) {
            threadPool.execute(() -> {
                try {
                    // 保证并发数
                    semaphore.acquire();
                    Jedis jedis = jedisPool.getResource();
                    Integer count = Integer.parseInt(jedis.get(KEY));
                    count++;
                    jedis.set(KEY, count.toString());
                    jedis.close();
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
        System.out.println("result: " + mainJedis.get(KEY) + ", execute time: " + (endTime - startTime) + "ms");
        mainJedis.close();

    }
}
