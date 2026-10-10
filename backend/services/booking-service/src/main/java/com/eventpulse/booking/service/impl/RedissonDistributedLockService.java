package com.eventpulse.booking.service.impl;

import com.eventpulse.booking.exception.ConcurrentBookingException;
import com.eventpulse.booking.service.DistributedLockService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
public class RedissonDistributedLockService implements DistributedLockService {

    private final RedissonClient redissonClient;

    @Autowired
    public RedissonDistributedLockService(@Nullable RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public <T> T executeWithLock(String lockKey, long waitSeconds, long leaseSeconds, Supplier<T> task) {
        if (redissonClient == null) {
            log.debug("RedissonClient is null (e.g. test profile), bypassing distributed lock for key: {}", lockKey);
            return task.get();
        }

        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(waitSeconds, leaseSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ConcurrentBookingException("Thread interrupted while waiting for lock on: " + lockKey);
        }

        if (!acquired) {
            log.warn("High concurrency contention: could not acquire lock for key: {}", lockKey);
            throw new ConcurrentBookingException("High demand detected for " + lockKey + ". Please retry your reservation.");
        }

        try {
            return task.get();
        } finally {
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (Exception ex) {
                log.warn("Error releasing lock for key {}: {}", lockKey, ex.getMessage());
            }
        }
    }
}
