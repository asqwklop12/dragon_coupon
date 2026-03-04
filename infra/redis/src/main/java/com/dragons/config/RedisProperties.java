package com.dragons.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value = "datasource.redis")
public record RedisProperties(
    int database,
    RedisNodeInfo master,
    List<RedisNodeInfo> replicas
) {
}
