package com.sfc.redistest;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class LockRunner implements Runner {

    private static final String LOCK_NAME = "lockRunner";

    private static final int ACQUIRE_TIMEOUT = 5000;

    private static final int LOCK_TIMEOUT = 5000;

    @Override
    public void process(Jedis jedis, String KEY, AtomicInteger counter) throws InterruptedException {

        String lockId = null;
        while (lockId == null) {
            counter.incrementAndGet();
            lockId = acquireLockWithTimeout(jedis, LOCK_NAME, ACQUIRE_TIMEOUT, LOCK_TIMEOUT);
        }

        Integer count = Integer.parseInt(jedis.get(KEY));
        count++;
        jedis.set(KEY, count.toString());

        releaseLock(jedis, LOCK_NAME, lockId);
    }

    /**
     * acquire lock with time out
     *
     * @param conn           Jedis instance
     * @param lockName       name of the lock, lock key
     * @param acquireTimeout acquire timeout in ms
     * @param lockTimeout    lock timeout in ms
     * @return lock identifier as lock value, null indicates that the lock was not acquired
     */
    public String acquireLockWithTimeout(Jedis conn, String lockName, long acquireTimeout, long lockTimeout) {
        String identifier = UUID.randomUUID().toString();
        String lockKey = "lock:" + lockName;
        int lockExpire = (int) (lockTimeout / 1000);

        long end = System.currentTimeMillis() + acquireTimeout;
        while (System.currentTimeMillis() < end) {
            if (conn.setnx(lockKey, identifier) == 1) {
                conn.expire(lockKey, lockExpire);
                return identifier;
            }
            if (conn.ttl(lockKey) == -1) {
                conn.expire(lockKey, lockExpire);
            }

            try {
                Thread.sleep(5, new Random().nextInt(900));
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        // null indicates that the lock was not acquired
        return null;
    }

    /**
     * release lock
     *
     * @param conn       Jedis instance
     * @param lockName   name of the lock, lock key
     * @param identifier lock identifier as lock value
     * @return
     */
    public boolean releaseLock(Jedis conn, String lockName, String identifier) {
        String lockKey = "lock:" + lockName;

        while (true) {
            conn.watch(lockKey);
            if (identifier.equals(conn.get(lockKey))) {
                Transaction trans = conn.multi();
                trans.del(lockKey);
                List<Object> results = trans.exec();
                if (results == null) {
                    continue;
                }
                return true;
            }

            conn.unwatch();
            break;
        }

        return false;
    }
}
