package com.eventpulse.booking.config;

import com.eventpulse.booking.service.DistributedLockService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.function.Supplier;

@TestConfiguration
@Profile("test")
public class TestRedisConfig {

    @Bean
    @Primary
    public DistributedLockService testDistributedLockService() {
        return new DistributedLockService() {
            @Override
            public <T> T executeWithLock(String lockKey, long waitSeconds, long leaseSeconds, Supplier<T> task) {
                return task.get();
            }
        };
    }
}
