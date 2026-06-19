package com.example.concurrencylab;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfig {

    @Bean
    public HazelcastInstance hazelcastInstance() {
        Config config = new Config();
        config.setClusterName("concurrencylab-cluster");

        config.addMapConfig(
                new MapConfig("products")
                        .setTimeToLiveSeconds(600)
        );

        return Hazelcast.newHazelcastInstance(config);
    }
}