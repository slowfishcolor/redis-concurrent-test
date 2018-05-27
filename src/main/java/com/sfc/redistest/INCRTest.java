package com.sfc.redistest;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class INCRTest implements TestBase {

    @Override
    public void runTest(int threadTotal, int requestTotal) throws InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(threadTotal);
        final CountDownLatch countDownLatch = new CountDownLatch(requestTotal);

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(threadTotal + 1);
        final JedisPool jedisPool = new JedisPool(config,"localhost");
        Jedis mainJedis= jedisPool.getResource();
        mainJedis.set(KEY, "0");

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < requestTotal; i++) {
            threadPool.execute(() -> {
                Jedis jedis = jedisPool.getResource();
                jedis.incr(KEY);
                jedis.close();
                countDownLatch.countDown();
            });
        }

        countDownLatch.await();
        threadPool.shutdown();

        long endTime = System.currentTimeMillis();
        System.out.println("result: " + mainJedis.get(KEY) + ", execute time: " + (endTime - startTime) + "ms");
        mainJedis.close();
    }

    public static void main(String[] args) throws InterruptedException {
        new INCRTest().runTest(DEFAULT_THREAD_TOTAL, DEFAULT_REQUEST_TOTAL);
    }
}
