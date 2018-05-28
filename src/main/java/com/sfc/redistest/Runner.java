package com.sfc.redistest;

import redis.clients.jedis.Jedis;

import java.util.concurrent.atomic.AtomicInteger;

public interface Runner {

    void process(final Jedis jedis, String KEY, AtomicInteger counter) throws InterruptedException;
}
