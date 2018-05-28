package com.sfc.redistest;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class CASRunner implements Runner{

    @Override
    public void process(Jedis jedis, String KEY, AtomicInteger counter) throws InterruptedException {

        while (true) {
            counter.incrementAndGet();

            jedis.watch(KEY);

            Integer count = Integer.parseInt(jedis.get(KEY));
            count++;

            Transaction transaction = jedis.multi();
            transaction.set(KEY, count.toString());
            List<Object> result = transaction.exec();

            if (result != null && result.size() != 0) {
                jedis.unwatch();
                return;
            }

            Thread.sleep(20, new Random().nextInt(900));
        }
    }
}
