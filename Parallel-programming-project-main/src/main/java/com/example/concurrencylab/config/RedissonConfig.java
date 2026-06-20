package com.example.concurrencylab.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    /**
     * يُنشئ RedissonClient bean واحد مشترك في كل التطبيق.
     * destroyMethod="shutdown" يضمن إغلاق الاتصال بـ Redis عند إيقاف التطبيق.
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
              .setAddress("redis://" + redisHost + ":" + redisPort)
              .setConnectionMinimumIdleSize(2)
              .setConnectionPoolSize(10)
              .setConnectTimeout(3000);
        return Redisson.create(config);
    }
}
