package com.firomsa.inventory.config;

import java.time.Duration;
import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

@Configuration
@EnableConfigurationProperties(CacheProperties.class)
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
public class CacheConfig {

    public static final String PRODUCTS = "products";
    public static final String LOW_STOCK = "lowStock";
    public static final String INVENTORY_VALUE = "inventoryValue";
    public static final String CATEGORIES = "categories";

    private final CacheProperties cacheProperties;

    public CacheConfig(CacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
    }

    @Bean
    RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        SerializationPair<Object> jsonSerializer = SerializationPair.fromSerializer(RedisSerializer.json());

        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeValuesWith(jsonSerializer);

        Map<String, RedisCacheConfiguration> caches = Map.of(
                PRODUCTS, base.entryTtl(Duration.ofMinutes(cacheProperties.getProducts())),
                LOW_STOCK, base.entryTtl(Duration.ofMinutes(cacheProperties.getLowStock())),
                INVENTORY_VALUE, base.entryTtl(Duration.ofMinutes(cacheProperties.getInventoryValue())),
                CATEGORIES, base.entryTtl(Duration.ofMinutes(cacheProperties.getCategories())));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(base.entryTtl(Duration.ofMinutes(cacheProperties.getProducts())))
                .withInitialCacheConfigurations(caches)
                .build();
    }
}
