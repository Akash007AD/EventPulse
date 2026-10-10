package com.eventpulse.booking.service;

import java.util.function.Supplier;

public interface DistributedLockService {

    <T> T executeWithLock(String lockKey, long waitSeconds, long leaseSeconds, Supplier<T> task);
}
