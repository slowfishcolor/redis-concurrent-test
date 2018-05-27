package com.sfc.redistest;

import redis.clients.jedis.Jedis;

import java.util.concurrent.atomic.AtomicInteger;

public class INCRRunner implements Runner {

    @Override
    public void process(Jedis jedis, String KEY, AtomicInteger counter) {
        jedis.incr(KEY);
        counter.incrementAndGet();
    }
}
