package com.sfc;

import com.sfc.redistest.*;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Hello world!
 *
 */
public class App 
{
    private static int THREAD_TOTAL = 100;

    private static int REQUEST_TOTAL = 10000;

    public static void main( String[] args ) throws InterruptedException {
        System.out.println( "Hello World!" );

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(REQUEST_TOTAL + 1);
        JedisPool jedisPool = new JedisPool(config, "localhost");

        System.out.println("INCR test start");
        ConcurrentBase.runTest(REQUEST_TOTAL, THREAD_TOTAL, jedisPool, new INCRRunner());
        System.out.println("INCR test end");

        System.out.println("Get and Set test start");
        ConcurrentBase.runTest(REQUEST_TOTAL, THREAD_TOTAL, jedisPool, new GetAndSetRunner());
        System.out.println("Get and Set test end");

        System.out.println("Compare and Set test start");
        ConcurrentBase.runTest(REQUEST_TOTAL, THREAD_TOTAL, jedisPool, new CASRunner());
        System.out.println("Compare and Set test end");

        System.out.println("Lock test start");
        ConcurrentBase.runTest(REQUEST_TOTAL, THREAD_TOTAL, jedisPool, new LockRunner());
        System.out.println("Lock test end");

        jedisPool.close();

    }

}
