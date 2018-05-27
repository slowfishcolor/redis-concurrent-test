package com.sfc.redistest;

import redis.clients.jedis.Jedis;

import java.util.concurrent.atomic.AtomicInteger;

public class GetAndSetRunner implements Runner{

    @Override
    public void process(Jedis jedis, String KEY, AtomicInteger counter) {
        Integer count = Integer.parseInt(jedis.get(KEY));
        count++;
        jedis.set(KEY, count.toString());
        counter.incrementAndGet();
    }
}
