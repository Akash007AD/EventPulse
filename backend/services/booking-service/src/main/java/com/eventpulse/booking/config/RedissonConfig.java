package com.eventpulse.booking.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:${REDIS_HOST:localhost}}")
    private String redisHost;

    @Value("${spring.data.redis.port:${REDIS_PORT:6379}}")
    private int redisPort;

    @Bean(destroyMethod = "shutdown")
    @Profile("!test")
    public RedissonClient redissonClient() {
        log.info("Initializing Redisson client connecting to redis://{}:{}", redisHost, redisPort);
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort)
                .setConnectTimeout(3000)
                .setTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(1000);
        return Redisson.create(config);
    }
}
